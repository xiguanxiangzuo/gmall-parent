package com.xgxz.gmall.cart.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 习惯向左
 * @create 2019-12-22 17:09
 */
@Data
public class CartResponse implements Serializable {

    private Cart cart;

    private CartItem cartItem;

    private String cartKey;
}
