package com.xgxz.gmall.to.es;

import com.xgxz.gmall.pms.entity.SkuStock;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author 习惯向左
 * @create 2019-12-05 23:20
 */
@Data
public class EsSkuProductInfo extends SkuStock implements Serializable {

    private String skuTitle;  // sku的特定标题
    /**
     * 每个sku不同的属性以及他的值
     */
    List<EsProductAttributeValue> attributeValues;
}
