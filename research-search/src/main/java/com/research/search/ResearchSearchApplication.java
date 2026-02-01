package com.research.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 搜索服务启动类
 */
@SpringBootApplication(scanBasePackages = "com.research")
@EnableDiscoveryClient
public class ResearchSearchApplication {
    public static void main(String[] args) {
        SpringApplication.run(ResearchSearchApplication.class, args);
    }
}
