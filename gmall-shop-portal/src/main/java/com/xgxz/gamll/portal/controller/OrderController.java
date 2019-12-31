package com.xgxz.gamll.portal.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.fastjson.JSON;
import com.xgxz.gmall.constant.SysCacheConstant;
import com.xgxz.gmall.oms.service.OrderService;
import com.xgxz.gmall.to.CommonResult;
import com.xgxz.gmall.ums.entity.Member;
import com.xgxz.gmall.vo.order.OrderConfirmVo;
import com.xgxz.gmall.vo.order.OrderCreateVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author 习惯向左
 * @create 2019-12-27 16:55
 */
@Slf4j
@Api(tags = {"订单服务"})
@RequestMapping("/order")
@RestController
public class OrderController {

    @Autowired
    StringRedisTemplate redisTemplate;

    @Reference
    OrderService orderService;

    @ApiOperation("订单确认")
    @GetMapping("/confirmOrder")
    public CommonResult confirmOrder(@RequestParam("accessToken") String accessToken){

        // 检查用户是否存在
        String memberJson = redisTemplate.opsForValue().get(SysCacheConstant.LOGIN_MEMBER + accessToken);

        if (StringUtils.isEmpty(accessToken) || StringUtils.isEmpty(memberJson)){
            // 未登录
            CommonResult result = new CommonResult().failed();
            result.setMessage("用户未登录，请先登录");
            return result;
        }

        Member member = JSON.parseObject(memberJson, Member.class);

        /*
         * 返回如下数据：
         *  1、当前用户的可选地址列表
         *  2、当前购物车选中的商品信息
         *  3、可用的优惠券信息
         *  4、支付、配送、发票方式信息
         */
        //dubbo 的RPC隐式传参；setAttachment() 保存一下下一个远程服务需要的参数
        RpcContext.getContext().setAttachment("accessToken",accessToken);
        // 调用下一个远程服务
        OrderConfirmVo confirm = orderService.orderConfirm(member.getId());

        return new CommonResult().success(confirm);
    }

    @ApiOperation("下单")
    @PostMapping("/create")
    public CommonResult createOrder(@RequestParam("totalPrice") BigDecimal totalPrice,
                                    @RequestParam("accessToken") String accessToken,
                                    @RequestParam("addressId") Long addressId,
                                    @RequestParam(value = "note",required = false) String note,
                                    @RequestParam("orderToken") String orderToken){

        RpcContext.getContext().setAttachment("accessToken",accessToken);
        RpcContext.getContext().setAttachment("orderToken",orderToken);

        // 需要防重复提交
        OrderCreateVo orderCreateVo = orderService.createOrder(totalPrice,addressId,note);

        if (!StringUtils.isEmpty(orderCreateVo.getToken())){
            CommonResult result = new CommonResult().failed();
            result.setMessage(orderCreateVo.getToken());
            return result;
        }

        return new CommonResult().success(orderCreateVo);
    }

    @ApiOperation("支付宝支付")
    @ResponseBody
    @GetMapping(value = "/pay",produces = {"text/html"})
    public String pay(@RequestParam("orderSn") String orderSn,@RequestParam("accessToken") String accessToken){

        String result = orderService.pay(orderSn,accessToken);
        return result;
    }

    @ResponseBody
    @RequestMapping("/pay/async/success")
    public String paySuccess(HttpServletRequest request) throws UnsupportedEncodingException {
        log.debug("支付宝支付异步通知完成....");
        // 修改订单的状态
        // 支付宝收到了success说明处理完成，不会再通知

        Map<String, String> params = new HashMap<String, String>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            // 乱码解决，这段代码在出现乱码时使用
            valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            params.put(name, valueStr);
        }

        String result = orderService.resolvePayResult(params);
        return result;
    }
}
