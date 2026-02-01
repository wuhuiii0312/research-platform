package com.research.document;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 文档管理服务启动类
 */
@SpringBootApplication(scanBasePackages = "com.research")
@EnableDiscoveryClient
public class ResearchDocumentApplication {
    public static void main(String[] args) {
        SpringApplication.run(ResearchDocumentApplication.class, args);
    }
}
