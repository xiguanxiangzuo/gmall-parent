package com.xgxz.gmall.pms.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xgxz.gmall.pms.entity.Brand;
import com.xgxz.gmall.pms.mapper.BrandMapper;
import com.xgxz.gmall.pms.service.BrandService;
import org.springframework.stereotype.Component;

/**
 * <p>
 * 品牌表 服务实现类
 * </p>
 *
 * @author 习惯向左
 * @since 2019-11-29
 */
@Service
@Component
public class BrandServiceImpl extends ServiceImpl<BrandMapper, Brand> implements BrandService {

}
