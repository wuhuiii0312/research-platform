package com.research.auth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 认证服务启动类
 */
@SpringBootApplication(scanBasePackages = "com.research")
@EnableDiscoveryClient  // 注册到Nacos
@EnableFeignClients(basePackages = "com.research")
@MapperScan("com.research.auth.mapper")  // 扫描Mapper接口
public class ResearchAuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(ResearchAuthApplication.class, args);
    }
}