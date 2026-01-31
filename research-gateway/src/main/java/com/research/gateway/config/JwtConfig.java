package com.research.gateway.config;

import com.research.common.core.util.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 手动注册JwtUtil Bean（绕过扫描问题，100%生效）
 */
@Configuration
public class JwtConfig {

    /**
     * 手动创建JwtUtil实例并注册为Spring Bean
     */
    @Bean
    public JwtUtil jwtUtil() {
        return new JwtUtil(); // 直接new实例，无需依赖@Component扫描
    }
}