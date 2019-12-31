package com.xgxz.gmall.cart.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * @author 习惯向左
 * @create 2019-12-22 16:34
 */
@Configuration
public class RedissonConfig {

    @Bean
    RedissonClient redisson() throws IOException {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://192.168.52.129:6379");

        return Redisson.create(config);
    }
}
