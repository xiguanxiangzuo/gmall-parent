package com.xgxz.gmall.pms.service;

import com.xgxz.gmall.pms.entity.Brand;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xgxz.gmall.vo.PageInfoVo;

/**
 * <p>
 * 品牌表 服务类
 * </p>
 *
 * @author 习惯向左
 * @since 2019-11-29
 */
public interface BrandService extends IService<Brand> {

    PageInfoVo brandPageInfo(String keyword, Integer pageNum, Integer pageSize);
}
