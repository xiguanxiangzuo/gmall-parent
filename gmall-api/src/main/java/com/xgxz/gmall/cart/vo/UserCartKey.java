package com.xgxz.gmall.cart.vo;

import lombok.Data;

/**
 * @author 习惯向左
 * @create 2019-12-24 12:04
 */
@Data
public class UserCartKey {

    // 用户是否登录
    private boolean login;

    // 用户如果登录的 id
    private Long userId;

    // 用户没有登录而且没有购物车的临时购物车key
    private String tempCartKey;

    // 用户最终用哪个购物车
    private String finalCartKey;
}
