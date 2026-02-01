package com.research.gateway.filter;

import com.research.common.core.domain.CommonResult;
import com.research.common.core.util.JwtUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * 网关JWT认证过滤器（改用@Autowired注入，避免构造器问题）
 */
@Slf4j
@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    // 改用@Autowired注入，兼容所有场景
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    // 空构造器（必须有，否则Spring无法创建实例）
    public JwtAuthFilter() {
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        // 跳过无需认证的路径
        String path = request.getPath().toString();
        if (skipAuth(path)) {
            log.debug("跳过认证路径：{}", path);
            return chain.filter(exchange);
        }

        // 获取令牌
        String token = request.getHeaders().getFirst("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            log.warn("路径{}未携带有效令牌", path);
            return buildUnauthorizedResponse(response, "未携带令牌或格式错误（需以Bearer开头）");
        }

        // 截取令牌
        token = token.substring(7).trim();
        if (token.isEmpty()) {
            log.warn("路径{}令牌内容为空", path);
            return buildUnauthorizedResponse(response, "令牌内容为空，拒绝访问");
        }

        // 验证令牌（即使JwtUtil是手动new的，也能正常调用方法）
        try {
            if (!jwtUtil.validateToken(token)) {
                log.warn("路径{}令牌无效/过期", path);
                return buildUnauthorizedResponse(response, "令牌无效或已过期，请重新登录");
            }
        } catch (Exception e) {
            log.error("验证令牌失败", e);
            return buildUnauthorizedResponse(response, "令牌验证异常，请重新登录");
        }

        // 令牌验证通过，继续执行
        return chain.filter(exchange);
    }

    /**
     * 构建401未授权响应
     */
    private Mono<Void> buildUnauthorizedResponse(ServerHttpResponse response, String message) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        response.getHeaders().set("Content-Charset", StandardCharsets.UTF_8.name());

        CommonResult<?> result = CommonResult.unauthorized(message);
        try {
            byte[] jsonBytes = objectMapper.writeValueAsBytes(result);
            DataBuffer buffer = response.bufferFactory().wrap(jsonBytes);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("序列化响应失败", e);
            byte[] textBytes = message.getBytes(StandardCharsets.UTF_8);
            DataBuffer buffer = response.bufferFactory().wrap(textBytes);
            return response.writeWith(Mono.just(buffer));
        }
    }

    /**
     * 跳过认证的路径（与网关路由 /api/auth/** 对应）
     */
    private boolean skipAuth(String path) {
        return path.startsWith("/swagger-ui/")
                || path.startsWith("/v3/api-docs/")
                || path.startsWith("/actuator/health")
                || path.equals("/api/auth/login")
                || path.equals("/api/auth/register")
                || path.equals("/api/auth/captcha")
                || path.startsWith("/api/auth/login")
                || path.startsWith("/api/auth/register")
                || path.startsWith("/api/auth/captcha")
                || path.equals("/api/user/login")
                || path.startsWith("/api/user/login")
                || path.startsWith("/api/user/register")
                || path.startsWith("/api/user/captcha");
    }

    @Override
    public int getOrder() {
        return -100;
    }
}