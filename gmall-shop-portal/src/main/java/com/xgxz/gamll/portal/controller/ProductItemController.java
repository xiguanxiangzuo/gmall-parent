package com.xgxz.gamll.portal.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.xgxz.gmall.pms.service.ProductService;
import com.xgxz.gmall.to.info.ProductInfo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 习惯向左
 * @create 2019-12-16 15:36
 *
 * 商品详情
 */
@RestController
public class ProductItemController {

    @Reference
    ProductService productService;

    @GetMapping("/detail/{id}")
    public ProductInfo productInfo(@PathVariable("id") Long id){

        ProductInfo info = productService.productAllInfo(id);

        return info;
    }

    @GetMapping("/detail/sku/{id}")
    public ProductInfo productSkuInfo(@PathVariable("id") Long id){

        ProductInfo productInfo = productService.productSkuInfo(id);
        return productInfo;
    }
}
