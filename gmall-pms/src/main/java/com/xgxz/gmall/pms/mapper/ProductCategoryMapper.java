package com.xgxz.gmall.pms.mapper;

import com.xgxz.gmall.pms.entity.ProductCategory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xgxz.gmall.vo.product.PmsProductCategoryWithChildrenItem;

import java.util.List;

/**
 * <p>
 * 产品分类 Mapper 接口
 * </p>
 *
 * @author 习惯向左
 * @since 2019-11-29
 */
public interface ProductCategoryMapper extends BaseMapper<ProductCategory> {

    List<PmsProductCategoryWithChildrenItem> listCatalogWithChildren(Integer i);
}
