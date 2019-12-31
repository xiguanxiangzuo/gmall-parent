package com.xgxz.gmall.cart.service;

import com.xgxz.gmall.cart.vo.CartItem;
import com.xgxz.gmall.cart.vo.CartResponse;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author 习惯向左
 * @create 2019-12-22 16:50
 *
 * 购物车服务
 */
public interface CartService {

    /**
     * 加的购物项的详细信息
     * @param skuId
     * @param num
     * @param cartKey
     * @param accessToken
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    CartResponse addToCart(Long skuId,Integer num, String cartKey, String accessToken) throws ExecutionException, InterruptedException;

    /**
     * 修改购物项数量
     * @param skuId
     * @param num
     * @param cartKey
     * @param accessToken
     * @return
     */
    CartResponse updateCartItemNum(Long skuId, Integer num, String cartKey, String accessToken);

    /**
     * 获取购物车的所有数据
     * @param cartKey
     * @param accessToken
     * @return
     */
    CartResponse listCart(String cartKey, String accessToken);

    /**
     * 删除指定购物项
     * @param skuId
     * @param cartKey
     * @param accessToken
     * @return
     */
    CartResponse deleteCartItem(Long skuId, String cartKey, String accessToken);

    /**
     * 清空购物车
     * @param cartKey
     * @param accessToken
     * @return
     */
    CartResponse clearCart(String cartKey, String accessToken);

    /**
     * 购物车选中/不选中
     * @param skuIds
     * @param ops
     * @param cartKey
     * @param accessToken
     * @return
     */
    CartResponse checkCartItems(String skuIds, Integer ops, String cartKey, String accessToken);

    /**
     * 获取某个用户购物车中选中的商品
     * @param accessToken
     * @return
     */
    List<CartItem> getCartItemForOrder(String accessToken);

    /**
     * 清除购物车中已经下单的商品
     * @param accessToken
     * @param skuIds
     */
    void removeCartItem(String accessToken, List<Long> skuIds);
}
