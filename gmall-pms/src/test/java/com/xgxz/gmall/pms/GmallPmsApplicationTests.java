package com.xgxz.gmall.pms;

import com.xgxz.gmall.pms.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallPmsApplicationTests {

    @Autowired
    ProductService productService;

    @Autowired
    StringRedisTemplate redisTemplate;


    @Test
    public void contextLoads() {

        System.out.println(productService.getById(1));
    }

    @Test
    public void redisTemplate(){
        redisTemplate.opsForValue().set("hello","world");

        String hello = redisTemplate.opsForValue().get("hello");

        log.debug("存进的值为：{}",hello);
    }
}
