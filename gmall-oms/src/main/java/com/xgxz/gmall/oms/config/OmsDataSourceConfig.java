package com.xgxz.gmall.oms.config;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import io.shardingjdbc.core.api.MasterSlaveDataSourceFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ResourceUtils;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

/**
 * @author 习惯向左
 * @create 2019-11-28 19:42
 */
@Configuration
public class OmsDataSourceConfig {

    @Bean
    public DataSource dataSource() throws IOException, SQLException {

        File file = ResourceUtils.getFile("classpath:sharding-jdbc.yaml");
        DataSource dataSource = MasterSlaveDataSourceFactory.createDataSource(file);
        return dataSource;
    }

    /**
     * 分页插件
     */
    @Bean
    public PaginationInterceptor paginationInterceptor(){
        return new PaginationInterceptor();
    }
}
