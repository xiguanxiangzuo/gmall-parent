package com.xgxz.gmall.oms.service.impl;

import com.xgxz.gmall.oms.entity.Order;
import com.xgxz.gmall.oms.mapper.OrderMapper;
import com.xgxz.gmall.oms.service.OrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 订单表 服务实现类
 * </p>
 *
 * @author 习惯向左
 * @since 2019-11-29
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

}
