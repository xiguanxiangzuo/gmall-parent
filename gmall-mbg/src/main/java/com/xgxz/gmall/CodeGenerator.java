package com.xgxz.gmall;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.InjectionConfig;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.builder.ConfigBuilder;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.FileType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * @author 习惯向左
 * @create 2019-11-28 0:04
 */
public class CodeGenerator {

    public static void main(String[] args) {

        String moduleName = "pms";

        //1、 代码生成器
        AutoGenerator mpg = new AutoGenerator();

        //2、 全局配置
        GlobalConfig gc = new GlobalConfig();
        String projectPath = System.getProperty("user.dir");
        gc.setOutputDir("G:\\opt\\module\\JetBrains\\IdeaProjects\\workspace\\gmall-parent\\gmall-mbg" + "/src/main/java");
        gc.setAuthor("习惯向左");
        gc.setOpen(false); // 生成后是否打开资源管理器
        gc.setFileOverride(false); // 重新生成时文件是否覆盖
        gc.setServiceName("%sService"); // 去掉Service接口的首字母I
        gc.setIdType(IdType.AUTO); // 主键策略
        gc.setDateType(DateType.ONLY_DATE); // 定义生成的实体类中日期类型
        gc.setSwagger2(true); // 开启 Swagger2 模式
        gc.setBaseColumnList(true);
        gc.setBaseResultMap(true);

        mpg.setGlobalConfig(gc);

        //3、 数据源配置
        DataSourceConfig dsc = new DataSourceConfig();
        dsc.setUrl("jdbc:mysql://192.168.52.129:3307/gmall_"+moduleName+"?useUnicode=true&useSSL=false&characterEncoding=utf8");
        // dsc.setSchemaName("public");
        dsc.setDriverName("com.mysql.jdbc.Driver");
        dsc.setUsername("root");
        dsc.setPassword("root");
        dsc.setDbType(DbType.MYSQL);
        mpg.setDataSource(dsc);

        //4、 包配置
        PackageConfig pc = new PackageConfig();
        pc.setModuleName(moduleName);  // 模块名
        pc.setParent("com.xgxz.gmall");
        //pc.setController("controller");
        pc.setEntity("entity");
        pc.setService("service");
        pc.setMapper("mapper");
        mpg.setPackageInfo(pc);



        //5、 策略配置
        StrategyConfig strategy = new StrategyConfig();
        strategy.setInclude(moduleName + "_\\w*"); // 设置要映射的表名
        strategy.setNaming(NamingStrategy.underline_to_camel); // 数据库表映射到实体的命名策略

        strategy.setTablePrefix(pc.getModuleName() + "_"); // 设置表前缀不生成
        strategy.setEntityTableFieldAnnotationEnable(true); // 是否生成实体类时，生成字段注解

        strategy.setColumnNaming(NamingStrategy.underline_to_camel); // 数据库表字段映射到实体的命名策略
        //strategy.setSuperEntityClass("com.baomidou.ant.common.BaseEntity");

        strategy.setEntityLombokModel(true); // lombok 模型 @Accessors(chain = true) setter 链式操作
        strategy.setRestControllerStyle(true); // restful api 风格控制器
        // 公共父类
        //strategy.setSuperControllerClass("com.baomidou.ant.common.BaseController");
        // 写于父类中的公共字段
        //strategy.setSuperEntityColumns("id");

        //strategy.setInclude(scanner("表名，多个英文逗号分割").split(","));
        strategy.setControllerMappingHyphenStyle(true); // url 中驼峰转连字符
        //strategy.setTablePrefix(pc.getModuleName() + "_");
        mpg.setStrategy(strategy);
        mpg.setTemplateEngine(new FreemarkerTemplateEngine());

        //6、执行
        mpg.execute();
    }

}
