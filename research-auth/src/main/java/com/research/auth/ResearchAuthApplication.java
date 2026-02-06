package com.research.auth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 认证服务启动类
 */
@SpringBootApplication(
        scanBasePackages = "com.research",
        exclude = {
                SecurityAutoConfiguration.class,              // 关闭Security核心自动配置
                ManagementWebSecurityAutoConfiguration.class  // 关闭Actuator的安全自动配置
        }
)
@MapperScan(basePackages = {"com.research.auth.mapper", "com.research.common.core.mapper"})
@EnableDiscoveryClient // 启用服务发现
public class ResearchAuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(ResearchAuthApplication.class, args);
    }
}