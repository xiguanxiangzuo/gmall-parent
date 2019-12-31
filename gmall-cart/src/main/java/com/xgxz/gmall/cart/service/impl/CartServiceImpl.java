package com.xgxz.gmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.xgxz.gmall.cart.component.MemberComponent;
import com.xgxz.gmall.cart.service.CartService;
import com.xgxz.gmall.cart.vo.Cart;
import com.xgxz.gmall.cart.vo.CartItem;
import com.xgxz.gmall.cart.vo.CartResponse;
import com.xgxz.gmall.cart.vo.UserCartKey;
import com.xgxz.gmall.constant.CartConstant;
import com.xgxz.gmall.pms.entity.Product;
import com.xgxz.gmall.pms.entity.SkuStock;
import com.xgxz.gmall.pms.service.ProductService;
import com.xgxz.gmall.pms.service.SkuStockService;
import com.xgxz.gmall.ums.entity.Member;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author 习惯向左
 * @create 2019-12-22 16:51
 */
@Service
@Component
public class CartServiceImpl implements CartService {

    @Autowired
    MemberComponent memberComponent;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    RedisTemplate redisTemplate;

    @Reference
    SkuStockService skuStockService;

    @Reference
    ProductService productService;

    @Override
    public CartResponse addToCart(Long skuId,Integer num, String cartKey, String accessToken) throws ExecutionException, InterruptedException {
        //0、根据 accessToken 获取用户的id
        Member member = memberComponent.getMemberByAccessToken(accessToken);

        if (member != null && !StringUtils.isEmpty(cartKey)){
            // 合并
            mergeCart(cartKey,member.getId());
        }

        // 获取到用户能真正使用的购物车
        UserCartKey userCartKey = memberComponent.getCartKey(accessToken, cartKey);
        String finalCartKey = userCartKey.getFinalCartKey();
        CartItem cartItem = addItemToCart(skuId,num,finalCartKey);

        CartResponse cartResponse = new CartResponse();
        cartResponse.setCartItem(cartItem);

        // 设置临时购物车用的cartKey
        cartResponse.setCartKey(userCartKey.getTempCartKey());

        // 返回整个购物车，方便操作......
        cartResponse.setCart(listCart(cartKey,accessToken).getCart());
        return cartResponse;

    }

    /**
     * 修改购物项数量
     * @param skuId
     * @param num
     * @param cartKey
     * @param accessToken
     * @return
     */
    @Override
    public CartResponse updateCartItemNum(Long skuId, Integer num, String cartKey, String accessToken) {

        UserCartKey userCartKey = memberComponent.getCartKey(accessToken, cartKey);

        String finalCartKey = userCartKey.getFinalCartKey();

        RMap<String, String> map = redissonClient.getMap(finalCartKey);

        String json = map.get(skuId.toString());
        CartItem cartItem = JSON.parseObject(json, CartItem.class);
        cartItem.setCount(num);

        String jsonString = JSON.toJSONString(cartItem);
        map.put(skuId.toString(),jsonString);

        CartResponse cartResponse = new CartResponse();
        cartResponse.setCartItem(cartItem);
        return cartResponse;
    }

    /**
     * 获取购物车所有数据
     * @param cartKey
     * @param accessToken
     * @return
     */
    @Override
    public CartResponse listCart(String cartKey, String accessToken) {
        UserCartKey userCartKey = memberComponent.getCartKey(accessToken, cartKey);
        if (userCartKey.isLogin()){
            mergeCart(cartKey,userCartKey.getUserId());
        }

        // 查询出购物车数据
        String finalCartKey = userCartKey.getFinalCartKey();

        // 自动续期
        //redisTemplate.expire(finalCartKey,30L, TimeUnit.DAYS);
        RMap<String, String> map = redissonClient.getMap(finalCartKey);

        Cart cart = new Cart();
        List<CartItem> cartItems = new ArrayList<>();
        CartResponse cartResponse = new CartResponse();

        if (map != null && !map.isEmpty()){
            map.entrySet().forEach((item) -> {
                if (!item.getKey().equalsIgnoreCase(CartConstant.CART_CHECKED_KEY)){
                    String value = item.getValue();
                    CartItem cartItem = JSON.parseObject(value, CartItem.class);

                    cartItems.add(cartItem);
                }
            });
            cart.setCartItems(cartItems);
        } else {
            // 用户没有购物车，新建一个购物车
            cartResponse.setCartKey(userCartKey.getTempCartKey());
        }


        cartResponse.setCart(cart);

        return cartResponse;
    }

    /**
     * 删除指定购物项
     * @param skuId
     * @param cartKey
     * @param accessToken
     * @return
     */
    @Override
    public CartResponse deleteCartItem(Long skuId, String cartKey, String accessToken) {

        UserCartKey userCartKey = memberComponent.getCartKey(accessToken, cartKey);
        String finalCartKey = userCartKey.getFinalCartKey();

        // 维护购物项的checked状态
        checkItem(Arrays.asList(skuId),false,finalCartKey);

        RMap<String, String> map = redissonClient.getMap(finalCartKey);

        map.remove(skuId.toString());

        // 删除完以后，再把购物车返回出去
        CartResponse cartResponse = listCart(cartKey, accessToken);

        return cartResponse;
    }

    /**
     * 清空购物车
     * @param cartKey
     * @param accessToken
     * @return
     */
    @Override
    public CartResponse clearCart(String cartKey, String accessToken) {

        UserCartKey userCartKey = memberComponent.getCartKey(accessToken, cartKey);

        String finalCartKey = userCartKey.getFinalCartKey();

        RMap<String, String> map = redissonClient.getMap(finalCartKey);

        map.clear();

        CartResponse cartResponse = new CartResponse();
        return cartResponse;
    }

    /**
     * 购物车选中/不选中
     * @param skuIds
     * @param ops
     * @param cartKey
     * @param accessToken
     * @return
     */
    @Override
    public CartResponse checkCartItems(String skuIds, Integer ops, String cartKey, String accessToken) {

        //1、找到每个skuId对应的购物车中的json，把状态check改为 ops对应的值
        List<Long> skuIdsList = new ArrayList<>();

        UserCartKey userCartKey = memberComponent.getCartKey(accessToken, cartKey);
        String finalCartKey = userCartKey.getFinalCartKey();
        RMap<String, String> cart = redissonClient.getMap(finalCartKey);

        boolean checked = ops==1?true:false;

        // 修改整个购物车的状态
        if (!StringUtils.isEmpty(skuIds)){
            String[] ids = skuIds.split(",");
            for (String id : ids){
                long skuId = Long.parseLong(id);
                skuIdsList.add(skuId);

                if (cart != null && !cart.isEmpty()){
                    String jsonValue = cart.get(id);

                    CartItem cartItem = JSON.parseObject(jsonValue, CartItem.class);
                    cartItem.setCheck(checked);
                    // 覆盖redis原数据
                    cart.put(id,JSON.toJSONString(cartItem));
                }
            }
        }

        //2、为了快速找到那个被选中了，我们单独维护了数据  数组在map中用的key是 checked 值是 Set集合最好
        checkItem(skuIdsList,checked,finalCartKey);

        //3、返回整个购物车
        CartResponse cartResponse = listCart(cartKey, accessToken);
        return cartResponse;
    }

    /**
     * 获取某个用户购物车中选中的商品
     * @param accessToken
     * @return
     */
    @Override
    public List<CartItem> getCartItemForOrder(String accessToken) {
        List<CartItem> cartItems = new ArrayList<>();

        UserCartKey cartKey = memberComponent.getCartKey(accessToken, null);
        RMap<String, String> cart = redissonClient.getMap(cartKey.getFinalCartKey());

        String checkItemsJson = cart.get(CartConstant.CART_CHECKED_KEY);
        Set<Long> items = JSON.parseObject(checkItemsJson, new TypeReference<Set<Long>>() {});
        items.forEach((item) -> {
            String itemJson = cart.get(item.toString());
            cartItems.add(JSON.parseObject(itemJson,CartItem.class));
        });

        return cartItems;
    }

    @Override
    public void removeCartItem(String accessToken, List<Long> skuIds) {
        UserCartKey userCartKey = memberComponent.getCartKey(accessToken, null);

        String finalCartKey = userCartKey.getFinalCartKey();

        RMap<String, String> map = redissonClient.getMap(finalCartKey);

        skuIds.forEach((id) -> {
            map.remove(id.toString());
        });
        map.put(CartConstant.CART_CHECKED_KEY,JSON.toJSONString(new LinkedHashSet<Long>()));
    }

    private CartItem addItemToCart(Long skuId,Integer num,String finalCartKey) throws ExecutionException, InterruptedException {

        CartItem newCartItem = new CartItem();

        CompletableFuture<Void> skuFuture = CompletableFuture.supplyAsync(() -> {
            SkuStock skuStock = skuStockService.getById(skuId);
            return skuStock;
        }).thenAcceptAsync((stock) -> {
            Long productId = stock.getProductId();
            Product product = productService.getById(productId);

            BeanUtils.copyProperties(stock, newCartItem);
            newCartItem.setSkuId(stock.getId());
            newCartItem.setName(product.getName());
            newCartItem.setCount(num);
        });

        RMap<String, String> map = redissonClient.getMap(finalCartKey);
        String itemJson = map.get(skuId.toString());
        skuFuture.get(); // 在线等结果
        if (!StringUtils.isEmpty(itemJson)){
            // 数量叠加
            CartItem oldCartItem = JSON.parseObject(itemJson, CartItem.class);
            Integer count = oldCartItem.getCount();

            //等到异步任务完成了，newCartItem才能用
            newCartItem.setCount(count+1);

            String jsonString = JSON.toJSONString(newCartItem);
            map.put(skuId.toString(),jsonString);
        } else {
            // 新增购物项
            String jsonString = JSON.toJSONString(newCartItem);
            map.put(skuId.toString(),jsonString);
        }

        // 维护勾选状态列表
        checkItem(Arrays.asList(skuId),true,finalCartKey);

        return newCartItem;
    }

    /**
     *
     * @param cartKey 老购物车
     * @param id 用户id
     */
    private void mergeCart(String cartKey, Long id) {

        String oldCartKey = CartConstant.TEMP_CART_KEY_PREFIX + cartKey;
        String userCartKey = CartConstant.USER_CART_KEY_PREFIX + id.toString();

        // 获取老购物车的数据
        RMap<String, String> map = redissonClient.getMap(oldCartKey);

        // 将老购物车的数据添加到新购物车中
        if (map != null && !map.isEmpty()){
            // map不是null，并且不为空
            map.entrySet().forEach((item) ->{
                // skuId
                String key = item.getKey();
                if (!key.equalsIgnoreCase(CartConstant.CART_CHECKED_KEY)){
                    // 购物项的 json 数据
                    String value = item.getValue();
                    CartItem cartItem = JSON.parseObject(value, CartItem.class);
                    try {
                        addItemToCart(Long.parseLong(key),cartItem.getCount(),userCartKey);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        // 清掉老购物车的数据
        map.clear();
    }

    private void checkItem(List<Long> skuId,boolean checked,String finalCartKey){
        RMap<String, String> cart = redissonClient.getMap(finalCartKey);

        String checkedJson = cart.get(CartConstant.CART_CHECKED_KEY);
        Set<Long> longSet = JSON.parseObject(checkedJson, new TypeReference<Set<Long>>() {
        });

        // 防止空指针异常
        if (longSet == null || longSet.isEmpty()){
            longSet = new LinkedHashSet<>();
        }
        if (checked){
            longSet.addAll(skuId);
        } else {
            longSet.removeAll(skuId);
        }

        // 重新保存被选中的商品
        cart.put(CartConstant.CART_CHECKED_KEY,JSON.toJSONString(longSet));
    }
}
