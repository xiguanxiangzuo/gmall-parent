package com.xgxz.gmall.oms.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xgxz.gmall.cart.service.CartService;
import com.xgxz.gmall.cart.vo.CartItem;
import com.xgxz.gmall.constant.OrderStatusEnum;
import com.xgxz.gmall.constant.SysCacheConstant;
import com.xgxz.gmall.oms.component.MemberComponent;
import com.xgxz.gmall.oms.config.AlipayConfig;
import com.xgxz.gmall.oms.entity.Order;
import com.xgxz.gmall.oms.entity.OrderItem;
import com.xgxz.gmall.oms.mapper.OrderItemMapper;
import com.xgxz.gmall.oms.mapper.OrderMapper;
import com.xgxz.gmall.oms.service.OrderService;
import com.xgxz.gmall.pms.entity.SkuStock;
import com.xgxz.gmall.pms.service.ProductService;
import com.xgxz.gmall.pms.service.SkuStockService;
import com.xgxz.gmall.to.es.EsProductAttributeValue;
import com.xgxz.gmall.to.es.EsSkuProductInfo;
import com.xgxz.gmall.to.info.ProductInfo;
import com.xgxz.gmall.ums.entity.Member;
import com.xgxz.gmall.ums.entity.MemberReceiveAddress;
import com.xgxz.gmall.ums.service.MemberService;
import com.xgxz.gmall.vo.order.OrderConfirmVo;
import com.xgxz.gmall.vo.order.OrderCreateVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * <p>
 * 订单表 服务实现类
 * </p>
 *
 * @author 习惯向左
 * @since 2019-11-29
 */
@Slf4j
@Service
@Component
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Reference
    MemberService memberService;

    @Reference
    CartService cartService;

    @Reference
    ProductService productService;

    @Reference
    SkuStockService skuStockService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    MemberComponent memberComponent;

    @Autowired
    OrderMapper orderMapper;

    @Autowired
    OrderItemMapper orderItemMapper;

    ThreadLocal<List<CartItem>> threadLocal = new ThreadLocal<>();

    @Override
    public OrderConfirmVo orderConfirm(Long id) {

        // 获取上一步隐式传参带来的 accessToken
        String accessToken = RpcContext.getContext().getAttachment("accessToken");

        OrderConfirmVo confirmVo = new OrderConfirmVo();

        // 会员收货地址
        confirmVo.setAddresses(memberService.getMemberAddress(id));

        // 设置优惠券信息
        confirmVo.setCoupons(null);

        List<CartItem> cartItems = cartService.getCartItemForOrder(accessToken);
        // 设置购物项信息
        confirmVo.setItems(cartItems);

        String token = UUID.randomUUID().toString().replace("-", "");
        token = token+"_"+System.currentTimeMillis()+"_"+60 * 10;

        // 保存防重令牌
        redisTemplate.opsForSet().add(SysCacheConstant.ORDER_UNIQUE_TOKEN,token);

        // 设置订单防重令牌
        confirmVo.setOrderToken(token);

        // 计算价格等
        confirmVo.setCouponPrice(null);
        for (CartItem cartItem : cartItems) {
            Integer count = cartItem.getCount();
            confirmVo.setCount(confirmVo.getCount() + count);
            BigDecimal totalPrice = cartItem.getTotalPrice();
            confirmVo.setProductTotalPrice(confirmVo.getProductTotalPrice().add(totalPrice));
        }

        confirmVo.setTotalPrice(confirmVo.getProductTotalPrice().add(confirmVo.getTransPrice()));
        return confirmVo;
    }


    @Transactional
    @Override
    public OrderCreateVo createOrder(BigDecimal frontTotalPrice, Long addressId, String note) {

        //0、防重检查
        String orderToken = RpcContext.getContext().getAttachment("orderToken");
        if (StringUtils.isEmpty(orderToken)){
            OrderCreateVo orderCreateVo = new OrderCreateVo();
            orderCreateVo.setToken("此次操作出现错误，请重新尝试");
            return orderCreateVo;

        }
        String[] s = orderToken.split("_");
        if (s.length != 3){
            OrderCreateVo orderCreateVo = new OrderCreateVo();
            orderCreateVo.setToken("非法的操作，请重新尝试");
            return orderCreateVo;
        }
        long createTime = Long.parseLong(s[1]);
        long timeout = Long.parseLong(s[2]);
        if (System.currentTimeMillis()-createTime >= timeout){
            OrderCreateVo orderCreateVo = new OrderCreateVo();
            orderCreateVo.setToken("页面超时，请刷新");
            return orderCreateVo;
        }

        Long remove = redisTemplate.opsForSet().remove(SysCacheConstant.ORDER_UNIQUE_TOKEN, orderToken);
        if (remove == 0){
            // 令牌非法
            OrderCreateVo orderCreateVo = new OrderCreateVo();
            orderCreateVo.setToken("创建失败，请刷新重试");
            return orderCreateVo;
        }

        //1、获取到当前会员
        String accessToken = RpcContext.getContext().getAttachment("accessToken");

        //订单验价
        Boolean validPrice = validPrice(frontTotalPrice, accessToken, addressId);
        if (!validPrice){
            OrderCreateVo createVo = new OrderCreateVo();
            createVo.setLimit(false); // 比价失败
            return createVo;
        }

        Member member = memberComponent.getMemberByAccessToken(accessToken);

        // 构造订单 vo
        OrderCreateVo orderCreateVo = initOrderCreateVo(frontTotalPrice, addressId, accessToken, member);

        // 初始化订单
        Order order = initOrder(frontTotalPrice, addressId, note, orderCreateVo, member);

        // 保存订单
        orderMapper.insert(order);

        // 构造/保存订单项信息  threadLocal 同一个线程共享数据
        savaOrderItem(order,accessToken);

        return orderCreateVo;
    }

    @Override
    public String pay(String orderSn, String accessToken) {

        Order order = orderMapper.selectOne(new QueryWrapper<Order>().eq("order_sn", orderSn));

        List<OrderItem> orderItems = orderItemMapper.selectList(new QueryWrapper<OrderItem>().eq("order_sn", orderSn));

        String productName = orderItems.get(0).getProductName();

        StringBuffer body = new StringBuffer();
        for(OrderItem item : orderItems){
            body.append(item.getProductName());
        }

        return payOrder(orderSn,order.getTotalAmount().toString(),"【习惯向左】- " + productName,body.toString());
    }

    @Override
    public String resolvePayResult(Map<String, String> params) {


        boolean signVerified = true;
        try {
            signVerified = AlipaySignature.rsaCheckV1(params, AlipayConfig.alipay_public_key, AlipayConfig.charset,
                    AlipayConfig.sign_type);
            System.out.println("验签：" + signVerified);

        } catch (AlipayApiException e) {
            // TODO Auto-generated catch block
        }
        // 商户订单号
        String out_trade_no = params.get("out_trade_no");
        // 支付宝流水号
        String trade_no = params.get("trade_no");
        // 交易状态
        String trade_status = params.get("trade_status");

        if (trade_status.equals("TRADE_FINISHED")) {
            //改订单状态
            log.debug("订单【{}】,已经完成...不能再退款。数据库都改了",out_trade_no);
        } else if (trade_status.equals("TRADE_SUCCESS")) {
            log.debug("订单【{}】,已经支付成功...可以退款。数据库都改了",out_trade_no);
        }


        return "success";
    }

    private void savaOrderItem(Order order,String accessToken) {
        List<Long> skuIds = new ArrayList<>();

        List<CartItem> cartItems = threadLocal.get();

        List<OrderItem> orderItems = new ArrayList<>();

        cartItems.forEach((cartItem) ->{
            skuIds.add(cartItem.getSkuId());
            OrderItem orderItem = new OrderItem();

            orderItem.setOrderId(order.getId());
            orderItem.setOrderSn(order.getOrderSn());
            Long skuId = cartItem.getSkuId();
            ProductInfo productInfo = productService.productSkuInfo(skuId);

            List<EsSkuProductInfo> skuProductInfos = productInfo.getSkuProductInfos();
            SkuStock skuStock = new SkuStock();
            String attrValueJsonStr = "";
            for (EsSkuProductInfo skuProductInfo : skuProductInfos){
                if (skuId == skuProductInfo.getId()){
                    List<EsProductAttributeValue> attributeValues = skuProductInfo.getAttributeValues();
                    attrValueJsonStr = JSON.toJSONString(attributeValues);

                    BeanUtils.copyProperties(skuProductInfo,skuStock);
                }
            }

            //SkuStock skuStock = productService.skuInfoById(skuId);

            orderItem.setProductId(productInfo.getId());
            orderItem.setProductPic(productInfo.getPic());
            orderItem.setProductName(productInfo.getName());
            orderItem.setProductBrand(productInfo.getBrandName());
            orderItem.setProductSn(productInfo.getProductSn());
            // 当前购物项的价格
            orderItem.setProductPrice(cartItem.getPrice());
            orderItem.setProductQuantity(cartItem.getCount());
            orderItem.setProductSkuId(skuId);
            orderItem.setProductSkuCode(skuStock.getSkuCode());
            orderItem.setProductCategoryId(productInfo.getProductCategoryId());
            orderItem.setSp1(skuStock.getSp1());
            orderItem.setSp2(skuStock.getSp2());
            orderItem.setSp3(skuStock.getSp3());

            orderItem.setProductAttr(attrValueJsonStr);

            orderItems.add(orderItem);

            orderItemMapper.insert(orderItem);

        });

        // 清除购物车中已经下单的商品
        cartService.removeCartItem(accessToken,skuIds);
    }

    /**
     * 构造订单 vo
     * @param frontTotalPrice
     * @param addressId
     * @param accessToken
     * @param member
     * @return
     */
    private OrderCreateVo initOrderCreateVo(BigDecimal frontTotalPrice, Long addressId, String accessToken, Member member) {
        OrderCreateVo orderCreateVo = new OrderCreateVo();

        String timeId = IdWorker.getTimeId();

        // 设置订单号
        orderCreateVo.setOrderSn(timeId);

        // 设置收货地址
        orderCreateVo.setAddressId(addressId);
        List<CartItem> cartItems = cartService.getCartItemForOrder(accessToken);

        // 设置购物车中的数据
        orderCreateVo.setCartItems(cartItems);

        // 设置会员id
        orderCreateVo.setMemberId(member.getId());

        // 设置总价格
        orderCreateVo.setTotalPrice(frontTotalPrice);

        // 描述信息
        orderCreateVo.setDetailInfo(cartItems.get(0).getName());
        return orderCreateVo;
    }

    private Order initOrder(BigDecimal frontTotalPrice, Long addressId, String note, OrderCreateVo orderCreateVo, Member member) {
        // 加工处理数据
        // 1、加工订单信息
        Order order = new Order();
        order.setMemberId(member.getId());
        order.setOrderSn(orderCreateVo.getOrderSn());
        order.setCreateTime(new Date());
        order.setAutoConfirmDay(7);
        order.setNote(note);
        order.setMemberUsername(member.getUsername());

        // 订单总额
        order.setTotalAmount(frontTotalPrice);
        order.setFreightAmount(new BigDecimal("10.00"));
        order.setStatus(OrderStatusEnum.UNPAY.getCode());

        // 设置收货人地址
        MemberReceiveAddress address = memberService.getMemberAddressByAddressId(addressId);
        order.setReceiverName(address.getName());
        order.setReceiverPhone(address.getPhoneNumber());
        order.setReceiverPostCode(address.getPostCode());
        order.setReceiverRegion(address.getRegion());
        order.setReceiverCity(address.getCity());
        order.setReceiverProvince(address.getProvince());
        order.setReceiverDetailAddress(address.getDetailAddress());
        return order;
    }

    private Boolean validPrice(BigDecimal frontPrice,String accessToken,Long addressId){
        // 拿到购物车
        List<CartItem> cartItems = cartService.getCartItemForOrder(accessToken);
        threadLocal.set(cartItems);
        BigDecimal bigDecimal = new BigDecimal("0");

        for (CartItem item : cartItems){
            //bigDecimal = bigDecimal.add(item.getTotalPrice());

            Long skuId = item.getSkuId();
            BigDecimal newPrice = skuStockService.getSkuPriceBySkuId(skuId);
            item.setPrice(newPrice);
            Integer count = item.getCount();
            // 当前项的总价
            BigDecimal multiply = newPrice.multiply(new BigDecimal(count.toString()));

            bigDecimal = bigDecimal.add(multiply);
        }

        // 根据收货地址计算运费

        BigDecimal tranPrice = new BigDecimal("10");

        BigDecimal totalPrice = bigDecimal.add(tranPrice);

        return totalPrice.compareTo(frontPrice) == 0 ? true : false;
    }


    // 支付宝支付
    private String payOrder(String out_trade_no, String total_amount, String subject, String body) {
        // 1、创建支付宝客户端
        AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfig.gatewayUrl, AlipayConfig.app_id,
                AlipayConfig.merchant_private_key, "json", AlipayConfig.charset, AlipayConfig.alipay_public_key,
                AlipayConfig.sign_type);

        // 2、创建一次支付请求
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(AlipayConfig.return_url);
        alipayRequest.setNotifyUrl(AlipayConfig.notify_url);

        // 商户订单号，商户网站订单系统中唯一订单号，必填
        // 付款金额，必填
        // 订单名称，必填
        // 商品描述，可空

        // 3、构造支付请求数据
        alipayRequest.setBizContent("{\"out_trade_no\":\"" + out_trade_no + "\"," + "\"total_amount\":\"" + total_amount
                + "\"," + "\"subject\":\"" + subject + "\"," + "\"body\":\"" + body + "\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        String result = "";
        try {
            // 4、请求
            result = alipayClient.pageExecute(alipayRequest).getBody();
        } catch (AlipayApiException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return result;// 支付跳转页的代码

    }

}
