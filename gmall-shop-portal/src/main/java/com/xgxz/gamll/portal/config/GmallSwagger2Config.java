package com.xgxz.gamll.portal.config;

import io.swagger.annotations.Api;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author 习惯向左
 * @create 2019-12-11 22:54
 */
@EnableSwagger2
@Configuration
public class GmallSwagger2Config {

    @Bean("检索模块")
    public Docket userApis(){
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("检索模块")
                .select()
                .apis(RequestHandlerSelectors.withClassAnnotation(Api.class))
                .paths(PathSelectors.regex("/search.*"))
                .build()
                .apiInfo(apiInfo())
                .enable(true);
    }

    private ApiInfo apiInfo(){
        return new ApiInfoBuilder()
                .title("习惯向左-检索平台接口文档")
                .description("提供检索模块的功能")
                .termsOfServiceUrl("http://www.google.com")
                .version("1.0")
                .build();
    }
}
