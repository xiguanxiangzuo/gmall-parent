package com.xgxz.gmall.ssoserver.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.xgxz.gmall.constant.SysCacheConstant;
import com.xgxz.gmall.to.CommonResult;
import com.xgxz.gmall.ums.entity.Member;
import com.xgxz.gmall.ums.service.MemberService;
import com.xgxz.gmall.vo.ums.LoginResponseVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.concurrent.TimeUnit;


/**
 * @author 习惯向左
 * @create 2019-12-22 13:46
 */
@RestController
public class LoginController {

    @Autowired
    StringRedisTemplate redisTemplate;

    @Reference
    MemberService memberService;

    @PostMapping("/applogin")
    public CommonResult login(@RequestParam("username") String username,
                              @RequestParam("password") String password){

        Member member = memberService.login(username,password);

        if (member == null){
            // 没有登录成功
            CommonResult result = new CommonResult().failed();
            result.setMessage("账号密码不匹配，请重新登录");
            return result;
        }else{
            String token = UUID.randomUUID().toString().replace("-", "");
            String memberJson = JSON.toJSONString(member);
            // 登录成功
            redisTemplate.opsForValue().set(SysCacheConstant.LOGIN_MEMBER+token,memberJson,
                    SysCacheConstant.LOGIN_MEMBER_TIMEOUT, TimeUnit.MINUTES);

            LoginResponseVo vo = new LoginResponseVo();
            BeanUtils.copyProperties(member,vo);
            vo.setAccessToken(token);
            return new CommonResult().success(vo);
        }
    }

    @GetMapping("/userInfo")
    public CommonResult getUserInfo(@RequestParam("accessToken") String accessToken){

        String member = redisTemplate.opsForValue().get(SysCacheConstant.LOGIN_MEMBER + accessToken);
        Member loginMember = JSON.parseObject(member, Member.class);
        loginMember.setId(null);
        loginMember.setPassword(null);
        return new CommonResult().success(loginMember);
    }
}
