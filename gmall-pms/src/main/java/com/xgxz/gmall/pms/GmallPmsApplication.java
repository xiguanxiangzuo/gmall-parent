package com.xgxz.gmall.pms;

import com.alibaba.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 1、配置整合 dubbo
 * 2、配置整合 MyBatisPlus
 *
 *
 * 事务的最终解决方案：
 *      1)、普通加事务，导入 jdbc-starter，@EnableTransactionManagement，加 @Transactional
 *      2)、方法自己调用自己类里面的加不上事务
 *          1>、导入aop包，开启代理对象的相关功能
 *              <dependency>
 *                  <groupId>org.springframework.boot</groupId>
 *                  <artifactId>spring-boot-starter-aop</artifactId>
 *              </dependency>
 *          2>、获取到当前类真正的代理对象，去掉方法即可
 *              1)、 @EnableAspectJAutoProxy(exposeProxy = true)：暴露代理对象
 *              2)、获取代理对象
 *
 */
@EnableDubbo
@EnableTransactionManagement
@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan(basePackages = "com.xgxz.gmall.pms.mapper")
@SpringBootApplication
public class GmallPmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmallPmsApplication.class, args);
    }

}
