package com.enterprise.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis 配置类
 * 扫描 Mapper 接口
 */
@Configuration
@MapperScan("com.enterprise.dao")
public class MyBatisConfig {
}
