package com.xgxz.gamll.portal.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.xgxz.gmall.search.SearchProductService;
import com.xgxz.gmall.vo.search.SearchParam;
import com.xgxz.gmall.vo.search.SearchResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 习惯向左
 * @create 2019-12-10 17:58
 *
 * 商品检索的 controller
 */
@Api(tags = "检索功能")
@RestController
public class ProductSearchController {

    @Reference
    SearchProductService searchProductService;

    @ApiOperation("商品检索")
    @GetMapping("/search")
    public SearchResponse productSearchResponse(SearchParam searchParam){

        SearchResponse searchResponse = searchProductService.searchProduct(searchParam);
        return searchResponse;
    }
}
