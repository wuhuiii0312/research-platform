package com.research.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * 网关启动类（仅扫描 gateway 包，JwtUtil 由 JwtConfig 手动注册；启用 Nacos 发现以使用 lb:// 路由）
 */
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@EnableDiscoveryClient
@ComponentScan(basePackages = "com.research.gateway")
public class ResearchGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ResearchGatewayApplication.class, args);
    }
}
