package com.research.gateway.config;

import com.research.common.core.util.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 手动注册 JwtUtil Bean（Gateway 不扫描 common 包，避免引入 WebMvc 等）
 */
@Configuration
public class JwtConfig {

    @Value("${jwt.secret:research-platform-jwt-secret-key-2026}")
    private String jwtSecret;

    @Value("${jwt.expire:7200}")
    private Long jwtExpire;

    @Bean
    public JwtUtil jwtUtil() {
        JwtUtil util = new JwtUtil();
        util.setSecret(jwtSecret);
        util.setExpire(jwtExpire);
        return util;
    }
}