package com.xgxz.gmall.admin.ums.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.xgxz.gmall.to.CommonResult;
import com.xgxz.gmall.ums.entity.MemberLevel;
import com.xgxz.gmall.ums.service.MemberLevelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author 习惯向左
 * @create 2019-12-02 16:28
 */
@CrossOrigin
@RestController
public class UmsMemberLevelController {

    @Reference
    MemberLevelService memberLevelService;

    /**
     * 查出所有会员等级
     * @return
     */
    @GetMapping("/memberLevel/list")
    public CommonResult memberLevelList(){

        List<MemberLevel> list = memberLevelService.list();
        return new CommonResult().success(list);
    }
}
