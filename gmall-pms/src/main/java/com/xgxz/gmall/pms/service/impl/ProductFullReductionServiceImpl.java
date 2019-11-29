package com.xgxz.gmall.pms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xgxz.gmall.pms.entity.ProductFullReduction;
import com.xgxz.gmall.pms.mapper.ProductFullReductionMapper;
import com.xgxz.gmall.pms.service.ProductFullReductionService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 产品满减表(只针对同商品) 服务实现类
 * </p>
 *
 * @author 习惯向左
 * @since 2019-11-29
 */
@Service
public class ProductFullReductionServiceImpl extends ServiceImpl<ProductFullReductionMapper, ProductFullReduction> implements ProductFullReductionService {

}
