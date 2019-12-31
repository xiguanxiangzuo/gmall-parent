package com.xgxz.gmall.ums.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xgxz.gmall.ums.entity.Admin;
import com.xgxz.gmall.ums.mapper.AdminMapper;
import com.xgxz.gmall.ums.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

/**
 * <p>
 * 后台用户表 服务实现类
 * </p>
 *
 * @author 习惯向左
 * @since 2019-11-29
 */
@Component
@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {

    @Autowired
    AdminMapper adminMapper;

    @Override
    public Admin login(String username, String password) {

        String pwd = DigestUtils.md5DigestAsHex(password.getBytes());

        QueryWrapper<Admin> wrapper = new QueryWrapper<Admin>().eq("username", username).eq("password", pwd);
        Admin admin = adminMapper.selectOne(wrapper);

        return admin;
    }

    @Override
    public Admin getUserInfo(String userName) {

        return adminMapper.selectOne(new QueryWrapper<Admin>().eq("username",userName));
    }
}
