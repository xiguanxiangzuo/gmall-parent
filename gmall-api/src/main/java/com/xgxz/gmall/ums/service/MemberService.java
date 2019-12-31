package com.xgxz.gmall.ums.service;

import com.xgxz.gmall.ums.entity.Member;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xgxz.gmall.ums.entity.MemberReceiveAddress;

import java.util.List;

/**
 * <p>
 * 会员表 服务类
 * </p>
 *
 * @author 习惯向左
 * @since 2019-11-29
 */
public interface MemberService extends IService<Member> {

    Member login(String username, String password);

    List<MemberReceiveAddress> getMemberAddress(Long id);

    MemberReceiveAddress getMemberAddressByAddressId(Long addressId);
}
