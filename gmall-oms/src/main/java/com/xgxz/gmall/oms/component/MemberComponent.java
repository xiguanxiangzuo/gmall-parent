package com.xgxz.gmall.oms.component;

import com.alibaba.fastjson.JSON;
import com.xgxz.gmall.constant.SysCacheConstant;
import com.xgxz.gmall.ums.entity.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * @author 习惯向左
 * @create 2019-12-27 23:57
 */
@Component
public class MemberComponent {

    @Autowired
    StringRedisTemplate redisTemplate;

    @Bean
    public Member getMemberByAccessToken(String accessToken){
        String json = redisTemplate.opsForValue().get(SysCacheConstant.LOGIN_MEMBER + accessToken);
        if (!StringUtils.isEmpty(json)){
            return JSON.parseObject(json,Member.class);
        }
        return null;
    }
}
