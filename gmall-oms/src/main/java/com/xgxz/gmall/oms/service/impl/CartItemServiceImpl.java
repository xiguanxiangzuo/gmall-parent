package com.xgxz.gmall.oms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xgxz.gmall.oms.entity.CartItem;
import com.xgxz.gmall.oms.mapper.CartItemMapper;
import com.xgxz.gmall.oms.service.CartItemService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 购物车表 服务实现类
 * </p>
 *
 * @author 习惯向左
 * @since 2019-11-29
 */
@Service
public class CartItemServiceImpl extends ServiceImpl<CartItemMapper, CartItem> implements CartItemService {

}
