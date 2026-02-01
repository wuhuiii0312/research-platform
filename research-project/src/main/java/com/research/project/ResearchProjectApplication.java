package com.research.project;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 项目管理服务启动类
 */
@SpringBootApplication(scanBasePackages = "com.research")
@EnableDiscoveryClient
@MapperScan("com.research.project.mapper")
public class ResearchProjectApplication {
    public static void main(String[] args) {
        SpringApplication.run(ResearchProjectApplication.class, args);
    }
}
