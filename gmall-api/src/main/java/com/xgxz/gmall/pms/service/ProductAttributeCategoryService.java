package com.xgxz.gmall.pms.service;

import com.xgxz.gmall.pms.entity.ProductAttributeCategory;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xgxz.gmall.vo.PageInfoVo;

/**
 * <p>
 * 产品属性分类表 服务类
 * </p>
 *
 * @author 习惯向左
 * @since 2019-11-29
 */
public interface ProductAttributeCategoryService extends IService<ProductAttributeCategory> {

    /**
     * 分页查询所有的属性分类
     * @param pageNum
     * @param pageSize
     * @return
     */
    PageInfoVo productAttributeCategoryPageInfo(Integer pageNum, Integer pageSize);
}
