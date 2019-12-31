package com.xgxz.gmall.pms.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mysql.cj.x.protobuf.MysqlxCrud;
import com.xgxz.gmall.constant.EsConstant;
import com.xgxz.gmall.pms.entity.*;
import com.xgxz.gmall.pms.mapper.*;
import com.xgxz.gmall.pms.service.ProductService;
import com.xgxz.gmall.to.es.EsProduct;
import com.xgxz.gmall.to.es.EsProductAttributeValue;
import com.xgxz.gmall.to.es.EsSkuProductInfo;
import com.xgxz.gmall.to.info.ProductInfo;
import com.xgxz.gmall.vo.PageInfoVo;
import com.xgxz.gmall.vo.product.PmsProductParam;
import com.xgxz.gmall.vo.product.PmsProductQueryParam;
import io.searchbox.client.JestClient;
import io.searchbox.core.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 商品信息 服务实现类
 * </p>
 *
 * @author 习惯向左
 * @since 2019-11-29
 */
@Slf4j
@Component
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Autowired
    ProductMapper productMapper;

    @Autowired
    ProductAttributeValueMapper productAttributeValueMapper;

    @Autowired
    ProductFullReductionMapper productFullReductionMapper;

    @Autowired
    ProductLadderMapper productLadderMapper;

    @Autowired
    SkuStockMapper skuStockMapper;

    @Autowired
    JestClient jestClient;

    // 当前线程共享同样的数据
    ThreadLocal<Long> threadLocal = new ThreadLocal<>();


    @Override
    public PageInfoVo productPageInfo(PmsProductQueryParam param) {

        QueryWrapper<Product> wrapper = new QueryWrapper<>();

        // 商品品牌 id
        if (param.getBrandId() != null){
            // 前台传了数据
            wrapper.eq("brand_id",param.getBrandId());
        }

        // 商品名称
        if (!StringUtils.isEmpty(param.getKeyword())){
            wrapper.like("name",param.getKeyword());
        }

        // 分类 id
        if (param.getProductCategoryId() != null){
            wrapper.eq("product_category_id",param.getProductCategoryId());
        }

        // 商品货号
        if (!StringUtils.isEmpty(param.getProductSn())){
            wrapper.like("product_sn",param.getProductSn());
        }

        // 发布状态
        if (param.getPublishStatus() != null){
            wrapper.eq("publish_status",param.getPublishStatus());
        }

        // 审核状态
        if(param.getVerifyStatus() != null){
            wrapper.eq("verify_status",param.getVerifyStatus());
        }

        IPage<Product> page = productMapper.selectPage(new Page<Product>(param.getPageNum(),
                param.getPageSize()), wrapper);

        PageInfoVo pageInfoVo = new PageInfoVo(page.getTotal(),page.getPages(),param.getPageSize(),page.getRecords(),page.getCurrent());

        return pageInfoVo;
    }

    /**
     * 大保存
     * @param productParam
     *
     * 事务的传播行为：当前方法的事务[是否要和别人公用一个事务]如何传播下去
     * @Transactional(propagation = )
     *
     *      REQUIRED：(必须)
     *          如果以前有事务，就和之前的事务公用一个事务，没有就创建一个事务
     *
     *      REQUIRES_NEW：(总是开启一个新事务)
     *          创建一个新的事务，如果以前有事务，暂停前面的事务
     *
     *      SUPPORTS：(支持)
     *          之前有事务，就以事务的方式运行，没有事务也可以
     *
     *      MANDATORY：(强制)
     *          一定要有事务，如果没有事务就报错
     *
     *      NOT_SUPPORTED：(不支持)
     *          不支持在事务内运行，如果已经有事务了，就挂起当前存在的事务
     *
     *      NEVER：(从不使用)
     *          不支持在事务内运行，如果已经有事务了，抛异常
     *
     *      NESTED：()
     *          开启一个子事务(MySQL 不支持),需要支持还原点的数据库
     *
     *   总结：
     *      传播行为过程中，只要REQUIRES_NEW被执行过就一定成功，不管后面出不出现问题。
     *      异常机制还是一样的，出现异常代码以后不执行。REQUIRED只要感觉到异常就一定回滚。和外事务
     *      是什么传播行为无关。
     */
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void saveProduct(PmsProductParam productParam) {

        ProductServiceImpl proxy = (ProductServiceImpl) AopContext.currentProxy();

        // 1、pms_product：保存商品基本信息
        proxy.saveBaseInfo(productParam);

        // 2、pms_product_attribute_value：保存这个商品对应的所有属性的值
        proxy.saveProductAttributeValue(productParam);

        // 3、pms_product_full_reduction：保存商品的满减信息
        proxy.saveFullReduction(productParam);

        // 4、pms_product_ladder：阶梯价格
        proxy.savaProductLadder(productParam);

        // 5、pms_sku_stock：库存表
        proxy.saveSkuStock(productParam);
    }

    /**
     * 批量上下架
     * @param ids
     * @param publishStatus
     */
    @Override
    public void updatePublishStatus(List<Long> ids, Integer publishStatus) {

        if (publishStatus == 0){
            // 下架  改数据库状态   删es
            ids.forEach((id) -> {
                setProductPublishStatus(publishStatus, id);

                deleteProductFromEs(id);
            });
        } else {
            // 上架  改数据库状态   加es

            // 1、对数据库是改变商品的状态
            ids.forEach((id) -> {
                setProductPublishStatus(publishStatus, id);

                saveProductToEs(id);

            });
        }
    }

    private void deleteProductFromEs(Long id) {

        try {
            Delete delete = new Delete
                    .Builder(id.toString())
                    .index(EsConstant.PRODUCT_ES_INDEX)
                    .type(EsConstant.PRODUCT_ES_INFO_TYPE)
                    .build();
            DocumentResult execute = jestClient.execute(delete);
            if (execute.isSucceeded()){
                log.info("ES 商品下架成功");
            } else {
                //deleteProductFromEs(id);
                log.error("ES id为{}的商品下架失败",id);
            }

        } catch (IOException e) {
            //deleteProductFromEs(id);
            log.error("ES id为{}的商品下架失败",id);
        }
    }

    private void saveProductToEs(Long id) {
        Product productInfo = productInfo(id);

        EsProduct esProduct = new EsProduct();
        BeanUtils.copyProperties(productInfo,esProduct);

        // 2、对es要保存商品信息
        List<SkuStock> stocks = skuStockMapper.selectList(new QueryWrapper<SkuStock>().eq("product_id", id));
        List<EsSkuProductInfo> skuProductInfos = new ArrayList<>(stocks.size());


        List<ProductAttribute> skuAttributeNames = productAttributeValueMapper.selectProductSaleAttrName(id);

        stocks.forEach((skuStock) -> {
            EsSkuProductInfo info = new EsSkuProductInfo();
            BeanUtils.copyProperties(skuStock,info);

            String subTitle = esProduct.getName();
            if (!StringUtils.isEmpty(skuStock.getSp1())){
                subTitle+=" "+skuStock.getSp1();
            }
            if (!StringUtils.isEmpty(skuStock.getSp2())){
                subTitle+=" "+skuStock.getSp2();
            }
            if (!StringUtils.isEmpty(skuStock.getSp3())){
                subTitle+=" "+skuStock.getSp3();
            }
            info.setSkuTitle(subTitle);

            List<EsProductAttributeValue> skuAttributeValues = new ArrayList<>();

            for (int i = 0;i < skuAttributeNames.size();i++){
                EsProductAttributeValue value = new EsProductAttributeValue();

                value.setName(skuAttributeNames.get(i).getName());
                value.setProductId(id);
                value.setProductAttributeId(skuAttributeNames.get(i).getId());
                value.setType(skuAttributeNames.get(i).getType());
                if (i == 0){
                    value.setValue(skuStock.getSp1());
                }
                if (i == 1){
                    value.setValue(skuStock.getSp2());
                }
                if (i == 2){
                    value.setValue(skuStock.getSp3());
                }

                skuAttributeValues.add(value);
            }


            info.setAttributeValues(skuAttributeValues);
            skuProductInfos.add(info);
        });
        esProduct.setSkuProductInfos(skuProductInfos);
        // 商品的公共属性
        List<EsProductAttributeValue> attributeValues = productAttributeValueMapper.selectProductBaseAttrAndValue(id);
        esProduct.setAttrValueList(attributeValues);

        try {
            Index build = new Index
                    .Builder(esProduct)
                    .index(EsConstant.PRODUCT_ES_INDEX)
                    .type(EsConstant.PRODUCT_ES_INFO_TYPE)
                    .id(id.toString())
                    .build();

            DocumentResult execute = jestClient.execute(build);
            boolean succeeded = execute.isSucceeded();
            if(succeeded){
                log.info("ES中，id为{}商品上架完成",id);
            } else {
                log.info("ES中，id为{}商品未保存成功",id);
               // saveProductToEs(id);
            }

        } catch (IOException e) {
            log.error("ES中，id为{}商品保存异常，{}",id, e.getMessage());
            //saveProductToEs(id);
        }

    }

    public void setProductPublishStatus(Integer publishStatus, Long id) {
        Product product = new Product();
        product.setId(id);
        product.setPublishStatus(publishStatus);
        productMapper.updateById(product);
    }

    @Override
    public Product productInfo(Long id) {
        return productMapper.selectById(id);
    }

    @Override
    public ProductInfo productAllInfo(Long id) {

        ProductInfo productInfo = null;

        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder.query(QueryBuilders.termQuery("id",id));

        Search build = new Search.Builder(builder.toString())
                .addIndex(EsConstant.PRODUCT_ES_INDEX)
                .addType(EsConstant.PRODUCT_ES_INFO_TYPE)
                .build();

        try {
            SearchResult execute = jestClient.execute(build);
            List<SearchResult.Hit<ProductInfo, Void>> hits = execute.getHits(ProductInfo.class);
            productInfo = hits.get(0).source;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return productInfo;
    }

    @Override
    public ProductInfo productSkuInfo(Long id) {

        ProductInfo productInfo = null;

        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder.query(QueryBuilders.nestedQuery("skuProductInfos",QueryBuilders.termQuery("skuProductInfos.id",id), ScoreMode.None));

        Search build = new Search.Builder(builder.toString())
                .addIndex(EsConstant.PRODUCT_ES_INDEX)
                .addType(EsConstant.PRODUCT_ES_INFO_TYPE)
                .build();

        try {
            SearchResult execute = jestClient.execute(build);
            List<SearchResult.Hit<ProductInfo, Void>> hits = execute.getHits(ProductInfo.class);
            productInfo = hits.get(0).source;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return productInfo;
    }

    @Override
    public SkuStock skuInfoById(Long skuId) {
        return skuStockMapper.selectById(skuId);
    }

    /**
     * 库存表
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveSkuStock(PmsProductParam productParam) {
        List<SkuStock> skuStockList = productParam.getSkuStockList();
        for (int i = 1; i <= skuStockList.size(); i++) {
            SkuStock skuStock = skuStockList.get(i-1);
            if(StringUtils.isEmpty(skuStock.getSkuCode())){
                skuStock.setSkuCode(threadLocal.get()+"_"+i);
            }
            skuStock.setProductId(threadLocal.get());
            skuStockMapper.insert(skuStock);
        }
    }

    /**
     * 阶梯价格
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void savaProductLadder(PmsProductParam productParam) {
        List<ProductLadder> productLadderList = productParam.getProductLadderList();
        productLadderList.forEach((productLadder) -> {
            productLadder.setProductId(threadLocal.get());
            productLadderMapper.insert(productLadder);
        });
    }

    /**
     * 保存商品的满减信息
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveFullReduction(PmsProductParam productParam) {
        List<ProductFullReduction> fullReductionList = productParam.getProductFullReductionList();
        fullReductionList.forEach((reduction)->{
            reduction.setProductId(threadLocal.get());
            productFullReductionMapper.insert(reduction);
        });
    }

    /**
     * 保存这个商品对应的所有属性的值
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveProductAttributeValue(PmsProductParam productParam) {
        List<ProductAttributeValue> valueList = productParam.getProductAttributeValueList();
        valueList.forEach((item)->{
            item.setProductId(threadLocal.get());
            productAttributeValueMapper.insert(item);
        });
    }

    /**
     * 保存商品基础信息
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveBaseInfo(PmsProductParam productParam){
        Product product = new Product();
        BeanUtils.copyProperties(productParam,product);
        productMapper.insert(product);
    }


}
