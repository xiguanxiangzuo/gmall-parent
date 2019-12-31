package com.xgxz.gmall.pms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xgxz.gmall.pms.entity.Product;
import com.xgxz.gmall.pms.entity.SkuStock;
import com.xgxz.gmall.to.info.ProductInfo;
import com.xgxz.gmall.vo.PageInfoVo;
import com.xgxz.gmall.vo.product.PmsProductParam;
import com.xgxz.gmall.vo.product.PmsProductQueryParam;

import java.util.List;

/**
 * <p>
 * 商品信息 服务类
 * </p>
 *
 * @author 习惯向左
 * @since 2019-11-29
 */
public interface ProductService extends IService<Product> {

    /**
     * 根据复杂查询条件返回分页数据
     * @param productQueryParam
     * @return
     */
    PageInfoVo productPageInfo(PmsProductQueryParam productQueryParam);

    /**
     * 保存商品信息
     * @param productParam
     */
    void saveProduct(PmsProductParam productParam);

    /**
     * 批量上下架
     * @param ids
     * @param publishStatus
     */
    void updatePublishStatus(List<Long> ids, Integer publishStatus);

    /**
     * 查询商品详情
     * @param id
     * @return
     */
    Product productInfo(Long id);

    ProductInfo productAllInfo(Long id);

    ProductInfo productSkuInfo(Long id);

    SkuStock skuInfoById(Long skuId);
}
