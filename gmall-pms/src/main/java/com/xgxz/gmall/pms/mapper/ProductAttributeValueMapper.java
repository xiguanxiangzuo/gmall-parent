package com.xgxz.gmall.pms.mapper;

import com.xgxz.gmall.pms.entity.ProductAttribute;
import com.xgxz.gmall.pms.entity.ProductAttributeValue;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xgxz.gmall.to.es.EsProductAttributeValue;

import java.util.List;

/**
 * <p>
 * 存储产品参数信息的表 Mapper 接口
 * </p>
 *
 * @author 习惯向左
 * @since 2019-11-29
 */
public interface ProductAttributeValueMapper extends BaseMapper<ProductAttributeValue> {

    List<EsProductAttributeValue> selectProductBaseAttrAndValue(Long id);

    List<ProductAttribute> selectProductSaleAttrName(Long id);
}
