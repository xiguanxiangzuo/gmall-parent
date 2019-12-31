package com.xgxz.gmall.pms.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xgxz.gmall.constant.SysCacheConstant;
import com.xgxz.gmall.pms.entity.ProductCategory;
import com.xgxz.gmall.pms.mapper.ProductCategoryMapper;
import com.xgxz.gmall.pms.service.ProductCategoryService;
import com.xgxz.gmall.vo.product.PmsProductCategoryWithChildrenItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * <p>
 * 产品分类 服务实现类
 * </p>
 *
 * @author 习惯向左
 * @since 2019-11-29
 */
@Slf4j
@Service
@Component
public class ProductCategoryServiceImpl extends ServiceImpl<ProductCategoryMapper, ProductCategory> implements ProductCategoryService {

    @Autowired
    ProductCategoryMapper productCategoryMapper;

    @Autowired
    RedisTemplate<Object,Object> redisTemplate;


    @Override
    public List<PmsProductCategoryWithChildrenItem> listCatalogWithChildren(Integer i) {

        Object cacheMenu = redisTemplate.opsForValue().get(SysCacheConstant.CATEGORY_MENU_CACHE_KEY);
        List<PmsProductCategoryWithChildrenItem> items;

        if (cacheMenu != null){
            // 缓存中有值
            log.debug("菜单数据命中缓存......");
            items = (List<PmsProductCategoryWithChildrenItem>)cacheMenu;
        } else {
            items = productCategoryMapper.listCatalogWithChildren(i);

            redisTemplate.opsForValue().set(SysCacheConstant.CATEGORY_MENU_CACHE_KEY,items);
        }

        return items;
    }
}
