package com.xgxz.gmall.ums.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xgxz.gmall.ums.entity.MemberProductCategoryRelation;
import com.xgxz.gmall.ums.mapper.MemberProductCategoryRelationMapper;
import com.xgxz.gmall.ums.service.MemberProductCategoryRelationService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 会员与产品分类关系表（用户喜欢的分类） 服务实现类
 * </p>
 *
 * @author 习惯向左
 * @since 2019-11-29
 */
@Service
public class MemberProductCategoryRelationServiceImpl extends ServiceImpl<MemberProductCategoryRelationMapper, MemberProductCategoryRelation> implements MemberProductCategoryRelationService {

}
