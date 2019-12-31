package com.xgxz.gmall.pms.service;

import com.xgxz.gmall.pms.entity.ProductAttribute;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xgxz.gmall.vo.PageInfoVo;

/**
 * <p>
 * 商品属性参数表 服务类
 * </p>
 *
 * @author 习惯向左
 * @since 2019-11-29
 */
public interface ProductAttributeService extends IService<ProductAttribute> {

    /**
     * 查询某个属性分类下的所有属性和参数
     * @param cid
     * @param type
     * @param pageSize
     * @param pageNum
     * @return
     */
    PageInfoVo getAttributes(Long cid, Integer type, Integer pageSize, Integer pageNum);
}
