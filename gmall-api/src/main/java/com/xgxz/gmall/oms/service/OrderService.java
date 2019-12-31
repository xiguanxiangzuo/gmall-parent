package com.xgxz.gmall.oms.service;

import com.xgxz.gmall.oms.entity.Order;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xgxz.gmall.vo.order.OrderConfirmVo;
import com.xgxz.gmall.vo.order.OrderCreateVo;

import java.math.BigDecimal;
import java.util.Map;

/**
 * <p>
 * 订单表 服务类
 * </p>
 *
 * @author 习惯向左
 * @since 2019-11-29
 */
public interface OrderService extends IService<Order> {

    OrderConfirmVo orderConfirm(Long id);

    OrderCreateVo createOrder(BigDecimal totalPrice, Long addressId,String note);

    String pay(String orderSn, String accessToken);

    String resolvePayResult(Map<String, String> params);
}
