package com.xgxz.gmall.cart.vo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author 习惯向左
 * @create 2019-12-22 15:48
 *
 * 购物车
 */
@Setter
public class Cart implements Serializable {

    @Getter
    List<CartItem> cartItems; // 所有的购物项

    private Integer count;  // 商品总数
    private BigDecimal totalPrice; // 已选中商品的总价

    public Integer getCount() {
        if (cartItems != null){
            AtomicInteger atomicInteger = new AtomicInteger(0);
            cartItems.forEach((cartItem) ->{
                atomicInteger.getAndAdd(cartItem.getCount());
            });
            return atomicInteger.get();
        } else {
            return 0;
        }
    }

    public BigDecimal getTotalPrice() {
        if (cartItems != null) {
            AtomicReference<BigDecimal> allTotal = new AtomicReference<>(new BigDecimal("0"));
            cartItems.forEach((cartItem) -> {
                BigDecimal add = allTotal.get().add(cartItem.getTotalPrice());
                allTotal.set(add);
            });
            return allTotal.get();
        } else {
            return new BigDecimal("0");
        }
    }
}
