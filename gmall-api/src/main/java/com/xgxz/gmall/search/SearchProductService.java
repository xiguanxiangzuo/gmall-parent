package com.xgxz.gmall.search;

import com.xgxz.gmall.vo.search.SearchParam;
import com.xgxz.gmall.vo.search.SearchResponse;

/**
 * @author 习惯向左
 * @create 2019-12-10 17:44
 *
 * 商品检索服务
 */
public interface SearchProductService {


    SearchResponse searchProduct(SearchParam searchParam);

}
