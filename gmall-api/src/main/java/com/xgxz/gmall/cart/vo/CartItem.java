package com.xgxz.gmall.cart.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author 习惯向左
 * @create 2019-12-22 15:36
 *
 * 购物项
 */
@Setter
public class CartItem implements Serializable {

    @Getter
    private Long skuId; //skuId

    @Getter
    private String name; // 商品名字

    @Getter
    private String skuCode;

    @Getter
    private BigDecimal price; // 价格

    @Getter
    private Integer stock; // 库存

    @Getter
    private String sp1;

    @Getter
    private String sp2;

    @Getter
    private String sp3;

    @Getter
    private String pic;

    @Getter
    private BigDecimal promotionPrice; // 促销价格

    @Getter
    private Integer count; // 该商品买了几个

    @Getter
    private boolean check = true;  // 购物项的选中状态

    private BigDecimal totalPrice; // 当前总价


    public BigDecimal getTotalPrice() {

        BigDecimal bigDecimal = price.multiply(new BigDecimal(count));

        return bigDecimal;
    }
}
