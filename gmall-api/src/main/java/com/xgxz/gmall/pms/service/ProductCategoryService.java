package com.xgxz.gmall.pms.service;

import com.xgxz.gmall.pms.entity.ProductCategory;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xgxz.gmall.vo.product.PmsProductCategoryWithChildrenItem;

import java.util.List;

/**
 * <p>
 * 产品分类 服务类
 * </p>
 *
 * @author 习惯向左
 * @since 2019-11-29
 */
public interface ProductCategoryService extends IService<ProductCategory> {

    /**
     * 查询这个菜单以及它的子菜单
     * @param i
     * @return
     */
    List<PmsProductCategoryWithChildrenItem> listCatalogWithChildren(Integer i);
}
