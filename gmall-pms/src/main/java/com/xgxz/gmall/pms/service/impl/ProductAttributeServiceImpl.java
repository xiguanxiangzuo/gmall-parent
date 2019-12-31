package com.xgxz.gmall.pms.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xgxz.gmall.pms.entity.ProductAttribute;
import com.xgxz.gmall.pms.mapper.ProductAttributeMapper;
import com.xgxz.gmall.pms.service.ProductAttributeService;
import com.xgxz.gmall.vo.PageInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * <p>
 * 商品属性参数表 服务实现类
 * </p>
 *
 * @author 习惯向左
 * @since 2019-11-29
 */
@Service
@Component
public class ProductAttributeServiceImpl extends ServiceImpl<ProductAttributeMapper, ProductAttribute> implements ProductAttributeService {

    @Autowired
    ProductAttributeMapper productAttributeMapper;


    @Override
    public PageInfoVo getAttributes(Long cid, Integer type, Integer pageSize, Integer pageNum) {

        QueryWrapper<ProductAttribute> queryWrapper = new QueryWrapper<ProductAttribute>()
                .eq("product_attribute_category_id", cid)
                .eq("type", type);
        IPage<ProductAttribute> iPage = productAttributeMapper.selectPage(new Page<ProductAttribute>(pageNum, pageSize), queryWrapper);

        return PageInfoVo.getPageVo(iPage,pageSize.longValue());
    }
}
