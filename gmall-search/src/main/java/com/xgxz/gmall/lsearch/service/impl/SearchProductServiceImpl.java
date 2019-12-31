package com.xgxz.gmall.lsearch.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.xgxz.gmall.constant.EsConstant;
import com.xgxz.gmall.search.SearchProductService;
import com.xgxz.gmall.to.es.EsProduct;
import com.xgxz.gmall.vo.search.SearchParam;
import com.xgxz.gmall.vo.search.SearchResponse;
import com.xgxz.gmall.vo.search.SearchResponseAttrVo;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.search.aggregation.ChildrenAggregation;
import io.searchbox.core.search.aggregation.MetricAggregation;
import io.searchbox.core.search.aggregation.TermsAggregation;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.swing.text.Highlighter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 习惯向左
 * @create 2019-12-10 17:45
 */
@Slf4j
@Service
@Component
public class SearchProductServiceImpl implements SearchProductService {

    @Autowired
    JestClient jestClient;


    @Override
    public SearchResponse searchProduct(SearchParam searchParam) {

        //1、构建检索条件
        String dsl = buildDsl(searchParam);

        log.error("商品检索出的数据{}",dsl);

        Search search = new Search.Builder(dsl).addIndex(EsConstant.PRODUCT_ES_INDEX)
                .addType(EsConstant.PRODUCT_ES_INFO_TYPE)
                .build();

        SearchResult execute = null;
        try {
            //2、检索
            execute = jestClient.execute(search);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 3、将返回的 SearchResult 转为 SearchResponse
        SearchResponse searchResponse = buildSearchResponse(execute);

        searchResponse.setPageNum(searchParam.getPageNum());
        searchResponse.setPageSize(searchParam.getPageSize());

        return searchResponse;
    }



    private String buildDsl(SearchParam searchParam) {

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        //1、查询
        //1.1)、检索
        if (!StringUtils.isEmpty(searchParam.getKeyword())){
            MatchQueryBuilder matchQuery = QueryBuilders.matchQuery("skuProductInfos.skuTitle", searchParam.getKeyword());

            NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("skuProductInfos", matchQuery, ScoreMode.None);

            boolQuery.must(nestedQuery);
        }
        //1.2)、过滤
        if (searchParam.getCatelog3() != null && searchParam.getCatelog3().length > 0){
            // 按照三级分类的条件过滤
            boolQuery.filter(QueryBuilders.termsQuery("productCategoryId",searchParam.getCatelog3()));
        }
        if (searchParam.getBrand() != null && searchParam.getBrand().length > 0){
            // 按照品牌的条件过滤
            boolQuery.filter(QueryBuilders.termsQuery("brandName.keyword",searchParam.getBrand()));
        }
        if (searchParam.getProps() != null && searchParam.getProps().length > 0){
            // 按照所有的筛选属性进行过滤

            String[] props = searchParam.getProps();
            for (String prop : props) {
                // 2:4g-3g
                String[] split = prop.split(":");

                BoolQueryBuilder must = QueryBuilders.boolQuery()
                        .must(QueryBuilders.matchQuery("attrValueList.productAttributeId", split[0]))
                        .must(QueryBuilders.termsQuery("attrValueList.value.keyword", split[1].split("-")));

                NestedQueryBuilder query = QueryBuilders.nestedQuery("attrValueList", must, ScoreMode.None);

                boolQuery.filter(query);
            }
        }
        if (searchParam.getPriceFrom() != null || searchParam.getPriceTo() != null){
            // 价格区间过滤

            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("price");

            if (searchParam.getPriceFrom() != null){
                rangeQuery.gte(searchParam.getPriceFrom());
            }

            if (searchParam.getPriceTo() != null){
                rangeQuery.lte(searchParam.getPriceTo());
            }

            boolQuery.filter(rangeQuery);
        }


        //1.2.1)、按照属性过滤、按照品牌过滤、按照分类过滤
        sourceBuilder.query(boolQuery);

        //2、高亮
        if (!StringUtils.isEmpty(searchParam.getKeyword())){
            HighlightBuilder highlightBuilder = new HighlightBuilder();

            highlightBuilder.field("skuProductInfos.skuTitle");
            highlightBuilder.preTags("<b style='color:red'>");
            highlightBuilder.postTags("</b>");

            sourceBuilder.highlighter(highlightBuilder);
        }


        //3、聚合
        // 按照品牌的
        TermsAggregationBuilder brand_agg = AggregationBuilders.terms("brand_agg").field("brandName.keyword");
        brand_agg.subAggregation(AggregationBuilders.terms("brandId").field("brandId"));
        sourceBuilder.aggregation(brand_agg);

        // 按照分类的
        TermsAggregationBuilder category_agg = AggregationBuilders.terms("category_agg").field("productCategoryName.keyword");
        category_agg.subAggregation(AggregationBuilders.terms("categoryId_agg").field("productCategoryId"));
        sourceBuilder.aggregation(category_agg);

        // 按照属性的
        NestedAggregationBuilder attr_agg = AggregationBuilders.nested("attr_agg", "attrValueList");
        TermsAggregationBuilder attrName_agg = AggregationBuilders.terms("attrName_agg").field("attrValueList.name");

        attrName_agg.subAggregation(AggregationBuilders.terms("attrValue_agg").field("attrValueList.value.keyword"));
        attrName_agg.subAggregation(AggregationBuilders.terms("attrId_agg").field("attrValueList.productAttributeId"));
        attr_agg.subAggregation(attrName_agg);

        sourceBuilder.aggregation(attr_agg);

        //4、分页
        sourceBuilder.from((searchParam.getPageNum()-1)*searchParam.getPageSize());
        sourceBuilder.size(searchParam.getPageSize());

        //5、排序
        if (!StringUtils.isEmpty(searchParam.getOrder())){

            String order = searchParam.getOrder();
            String[] split = order.split(":");
            if (split[0].equals("0")){
                // 综合排序，默认顺序
            }
            if (split[0].equals("1")){
                // 销量
                FieldSortBuilder sale = SortBuilders.fieldSort("sale");
                if (split[1].equalsIgnoreCase("asc")){
                    sale.order(SortOrder.ASC);
                } else {
                    sale.order(SortOrder.DESC);
                }
                sourceBuilder.sort(sale);
            }
            if (split[0].equals("2")){
                // 价格
                FieldSortBuilder price = SortBuilders.fieldSort("price");
                if (split[1].equalsIgnoreCase("asc")){
                    price.order(SortOrder.ASC);
                } else {
                    price.order(SortOrder.DESC);
                }
                sourceBuilder.sort(price);
            }
        }

        return sourceBuilder.toString();
    }

    private SearchResponse buildSearchResponse(SearchResult execute) {
        SearchResponse searchResponse = new SearchResponse();

        MetricAggregation aggregations = execute.getAggregations();

        //------------------------- 提取品牌信息 -------------------------------
        TermsAggregation brand_agg = aggregations.getTermsAggregation("brand_agg");
        List<String> brandNames = new ArrayList<>();
        brand_agg.getBuckets().forEach((bucket) ->{
            String keyAsString = bucket.getKeyAsString();
            brandNames.add(keyAsString);
        });
        SearchResponseAttrVo attrVo = new SearchResponseAttrVo();
        attrVo.setName("品牌");
        attrVo.setValue(brandNames);
        searchResponse.setBrand(attrVo);  // 可供选择的品牌
        //------------------------- 品牌信息提取完成 -------------------------------



        //------------------------- 提取分类信息 -------------------------------
        TermsAggregation category_agg = aggregations.getTermsAggregation("category_agg");
        List<String> categoryValues = new ArrayList<>();
        category_agg.getBuckets().forEach((bucket) ->{
            String categoryName = bucket.getKeyAsString();

            TermsAggregation categoryId_agg = bucket.getTermsAggregation("categoryId_agg");
            String categoryId = categoryId_agg.getBuckets().get(0).getKeyAsString();

            Map<String,String> map = new HashMap<>();
            map.put("id",categoryId);
            map.put("name",categoryName);
            String cateInfo = JSON.toJSONString(map);
            categoryValues.add(cateInfo);
        });

        SearchResponseAttrVo categoryAttrVo = new SearchResponseAttrVo();
        categoryAttrVo.setName("分类");
        categoryAttrVo.setValue(categoryValues);

        searchResponse.setCatelog(categoryAttrVo);// 可供选择的分类
        //------------------------- 分类信息提取完成 -------------------------------


        //------------------------- 提取属性信息 -------------------------------
        TermsAggregation termsAggregation = aggregations.getChildrenAggregation("attr_agg")
                .getTermsAggregation("attrName_agg");

        List<SearchResponseAttrVo> attrList = new ArrayList<>();

        termsAggregation.getBuckets().forEach((bucket) ->{
            SearchResponseAttrVo vo = new SearchResponseAttrVo();

            // 属性的名字
            String attrName = bucket.getKeyAsString();
            vo.setName(attrName);


            // 属性的id
            TermsAggregation attrIdAgg = bucket.getTermsAggregation("attrId_agg");
            String attrId = attrIdAgg.getBuckets().get(0).getKeyAsString();
            vo.setProductAttributeId(Long.parseLong(attrId));

            // 属性所涉及的所有值
            TermsAggregation attrValueAgg = bucket.getTermsAggregation("attrValue_agg");
            List<String> valueList = new ArrayList<>();
            attrValueAgg.getBuckets().forEach((valueBucket) -> {
                valueList.add(valueBucket.getKeyAsString());
            });
            vo.setValue(valueList);

            attrList.add(vo);
        });

        searchResponse.setAttrs(attrList);  // 所有可以筛选的属性
        //------------------------- 属性信息提取完成 -------------------------------

        //searchResponse.setPageNum();//
        //searchResponse.setPageSize();

        //-----------------------------封装商品记录------------------------------
        List<SearchResult.Hit<EsProduct, Void>> hits = execute.getHits(EsProduct.class);
        List<EsProduct> esProducts = new ArrayList<>();

        hits.forEach((hit) -> {
            EsProduct source = hit.source;
            // 提取到高亮结果
            String title = hit.highlight.get("skuProductInfos.skuTitle").get(0);
            source.setName(title);
            esProducts.add(source);
        });
        searchResponse.setProducts(esProducts); // 将查到的记录封装
        //-----------------------------商品记录封装完成------------------------------

        searchResponse.setTotal(execute.getTotal());


        return searchResponse;
    }
}
