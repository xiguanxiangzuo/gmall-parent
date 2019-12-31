package com.xgxz.gmall.ums.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xgxz.gmall.ums.entity.MemberLevel;
import com.xgxz.gmall.ums.mapper.MemberLevelMapper;
import com.xgxz.gmall.ums.service.MemberLevelService;
import org.springframework.stereotype.Component;

/**
 * <p>
 * 会员等级表 服务实现类
 * </p>
 *
 * @author 习惯向左
 * @since 2019-11-29
 */
@Component
@Service
public class MemberLevelServiceImpl extends ServiceImpl<MemberLevelMapper, MemberLevel> implements MemberLevelService {

}
