package com.xgxz.gmall.pms.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xgxz.gmall.pms.entity.SkuStock;
import com.xgxz.gmall.pms.mapper.SkuStockMapper;
import com.xgxz.gmall.pms.service.SkuStockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * <p>
 * sku的库存 服务实现类
 * </p>
 *
 * @author 习惯向左
 * @since 2019-11-29
 */
@Service
@Component
public class SkuStockServiceImpl extends ServiceImpl<SkuStockMapper, SkuStock> implements SkuStockService {

    @Autowired
    SkuStockMapper skuStockMapper;


    @Override
    public BigDecimal getSkuPriceBySkuId(Long skuId) {
        return skuStockMapper.selectById(skuId).getPrice();
    }
}
