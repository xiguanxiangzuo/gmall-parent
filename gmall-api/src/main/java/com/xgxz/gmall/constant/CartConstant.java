package com.xgxz.gmall.constant;

/**
 * @author 习惯向左
 * @create 2019-12-22 17:27
 */
public class CartConstant {

    public final static String TEMP_CART_KEY_PREFIX = "cart:temp:"; // 后面加 cartKey
    public final static String USER_CART_KEY_PREFIX = "cart:user:"; // 后面加 用户id

    // 购物车在 redis 中存储哪些被选中用的key
    public final static String CART_CHECKED_KEY = "checked";
}
