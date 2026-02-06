package com.research.notification;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(scanBasePackages = "com.research")
@EnableDiscoveryClient
@MapperScan("com.research.notification.mapper")
public class ResearchNotificationApplication {

    public static void main(String[] args) {
        SpringApplication.run(ResearchNotificationApplication.class, args);
    }
}
