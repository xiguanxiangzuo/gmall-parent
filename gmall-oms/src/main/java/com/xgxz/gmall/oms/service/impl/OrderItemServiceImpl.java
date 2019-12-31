package com.xgxz.gmall.oms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xgxz.gmall.oms.entity.OrderItem;
import com.xgxz.gmall.oms.mapper.OrderItemMapper;
import com.xgxz.gmall.oms.service.OrderItemService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 订单中所包含的商品 服务实现类
 * </p>
 *
 * @author 习惯向左
 * @since 2019-11-29
 */
@Service
public class OrderItemServiceImpl extends ServiceImpl<OrderItemMapper, OrderItem> implements OrderItemService {

}
