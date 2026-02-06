package com.research.auth.config; // 子包路径，自动被扫描

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration // 标记为配置类，Spring会扫描并创建Bean
public class SecurityConfig {
    
    // 定义PasswordEncoder Bean，解决注入缺失问题
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}