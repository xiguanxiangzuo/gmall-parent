package com.xgxz.gmall.vo.order;

import com.xgxz.gmall.cart.vo.CartItem;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author 习惯向左
 * @create 2019-12-27 23:36
 *
 * 结算页需要的 orderVo 数据
 */
@Data
public class OrderCreateVo implements Serializable {

    // 订单号
    private String orderSn;

    // 订单总额
    private BigDecimal totalPrice;

    // 用户的收货地址
    private Long addressId;

    // 详情描述
    private String detailInfo;

    // 会员的id
    private Long memberId;

    // 购买的商品
    private List<CartItem> cartItems;

    // 限制 验价
    private Boolean limit;

    // 令牌是否正确
    private String token;

}
