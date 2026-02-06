package com.research.auth.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类（生成、解析token）
 */
@Component
public class JwtUtils {

    // Token 过期时间：单位秒，如 7200=2 小时（内部会乘以 1000 转为毫秒）
    @Value("${jwt.expire:7200}")
    private long expire;

    // JWT密钥（和网关一致）
    @Value("${jwt.secret:research-platform-2025-secret-key-123456}")
    private String secret;

    /**
     * 生成Token
     */
    public String generateToken(Long userId, String username, String roleCode) {
        // 构建Claims（自定义载荷）
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("roleCode", roleCode);

        // 生成密钥
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());

        // 构建Token
        return Jwts.builder()
                .setClaims(claims)  // 自定义载荷
                .setIssuedAt(new Date())  // 签发时间
                .setExpiration(new Date(System.currentTimeMillis() + expire * 1000))  // 过期时间：配置为秒，此处转毫秒
                .signWith(key)  // 签名
                .compact();
    }

    /** 兼容旧调用：不传 roleCode 时默认 VISITOR */
    public String generateToken(Long userId, String username) {
        return generateToken(userId, username, "VISITOR");
    }

    /**
     * 解析Token，获取载荷
     */
    public Claims parseToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 从Token中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("userId", Long.class);
    }
}