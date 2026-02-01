package com.research.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.research.notification")
public class ResearchNotificationApplication {

    public static void main(String[] args) {
        SpringApplication.run(ResearchNotificationApplication.class, args);
    }
}
