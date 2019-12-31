package com.xgxz.gmall.vo.order;

import com.xgxz.gmall.cart.vo.CartItem;
import com.xgxz.gmall.sms.entity.Coupon;
import com.xgxz.gmall.ums.entity.MemberReceiveAddress;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author 习惯向左
 * @create 2019-12-27 17:02
 */
@Data
public class OrderConfirmVo implements Serializable {

    // 购物项
    private List<CartItem> items;

    // 地址列表
    private List<MemberReceiveAddress> addresses;

    // 优惠券
    List<Coupon> coupons;

    // 支付方式

    // 配送方式

    // 订单令牌，下一步提交订单必须带上
    private String orderToken;

    // 商品总额
    private BigDecimal productTotalPrice = new BigDecimal("0");

    // 订单总额
    private BigDecimal totalPrice = new BigDecimal("0");

    // 商品总数
    private Integer count = 0;

    // 优惠券减免
    private BigDecimal couponPrice = new BigDecimal("0");

    // 运费
    private BigDecimal transPrice = new BigDecimal("10");
}
