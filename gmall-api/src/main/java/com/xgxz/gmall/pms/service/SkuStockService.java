package com.xgxz.gmall.pms.service;

import com.xgxz.gmall.pms.entity.SkuStock;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;

/**
 * <p>
 * sku的库存 服务类
 * </p>
 *
 * @author 习惯向左
 * @since 2019-11-29
 */
public interface SkuStockService extends IService<SkuStock> {

    BigDecimal getSkuPriceBySkuId(Long skuId);
}
