package com.research.common.core.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis Plus 核心配置（简化版，适配3.5.3.1）
 * 说明：删除了复杂的自定义SqlInjector，新手先保证基础功能可用
 */
@Configuration
@MapperScan("com.research.common.mapper") // 根据你的实际mapper包路径调整
public class MybatisPlusConfig {

    /**
     * 分页插件（MyBatis Plus核心功能，保留）
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 添加分页插件，适配MySQL
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}