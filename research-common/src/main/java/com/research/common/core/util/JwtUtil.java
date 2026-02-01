package com.research.common.core.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component; // 关键注解：让Spring扫描为Bean

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * JWT工具类（必须加@Component，否则Spring无法创建Bean）
 */
@Slf4j
@Component // 核心：注册为Spring Bean
public class JwtUtil {

    // 从配置文件读取密钥（无配置时用默认值）；支持 setter 便于 Gateway 等非扫描场景手动注入
    @Value("${jwt.secret:research-platform-jwt-secret-key-2026}")
    private String secret;

    @Value("${jwt.expire:7200}")
    private Long expire;

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public void setExpire(Long expire) {
        this.expire = expire;
    }

    // 生成JWT令牌
    public String generateToken(Map<String, Object> claims) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expire * 1000))
                .addClaims(claims)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 解析JWT令牌
    public Claims parseToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.error("解析JWT失败：{}", e.getMessage(), e);
            return null;
        }
    }

    // 验证JWT是否有效
    public boolean validateToken(String token) {
        Claims claims = parseToken(token);
        return claims != null && !claims.getExpiration().before(new Date());
    }

    // 从Token中获取用户ID
    public String getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return claims == null ? null : claims.get("userId", String.class);
    }

    // 从Token中获取用户名
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims == null ? null : claims.get("username", String.class);
    }
}