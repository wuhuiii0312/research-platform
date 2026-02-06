package com.research.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
// 如果用注册中心，加这个注解
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

// 注册中心注解（有就加，没有则只保留 @SpringBootApplication）
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = "com.research")
public class ResearchUserApplication {
    public static void main(String[] args) {
        SpringApplication.run(ResearchUserApplication.class, args);
    }
}