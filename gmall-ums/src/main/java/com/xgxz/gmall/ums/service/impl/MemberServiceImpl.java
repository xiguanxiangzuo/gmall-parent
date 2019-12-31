package com.xgxz.gmall.ums.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xgxz.gmall.ums.entity.Member;
import com.xgxz.gmall.ums.entity.MemberReceiveAddress;
import com.xgxz.gmall.ums.mapper.MemberMapper;
import com.xgxz.gmall.ums.mapper.MemberReceiveAddressMapper;
import com.xgxz.gmall.ums.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.util.List;

/**
 * <p>
 * 会员表 服务实现类
 * </p>
 *
 * @author 习惯向左
 * @since 2019-11-29
 */
@Service
@Component
public class MemberServiceImpl extends ServiceImpl<MemberMapper, Member> implements MemberService {

    @Autowired
    MemberMapper memberMapper;

    @Autowired
    MemberReceiveAddressMapper addressMapper;

    @Override
    public Member login(String username, String password) {

        String digest = DigestUtils.md5DigestAsHex(password.getBytes());

        Member member = memberMapper.selectOne(new QueryWrapper<Member>()
                .eq("username", username)
                .eq("password", digest)
        );

        return member;
    }

    @Override
    public List<MemberReceiveAddress> getMemberAddress(Long id) {

        return addressMapper.selectList(new QueryWrapper<MemberReceiveAddress>().eq("member_id",id));
    }

    @Override
    public MemberReceiveAddress getMemberAddressByAddressId(Long addressId) {

        return addressMapper.selectById(addressId);
    }

}
