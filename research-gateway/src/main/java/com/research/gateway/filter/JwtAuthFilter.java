package com.research.gateway.filter;

import com.research.common.core.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 网关 JWT 鉴权：校验 token 并将 userId/username 写入请求头，供下游服务设置 SecurityUtils
 */
@Slf4j
@Component
public class JwtAuthFilter extends AbstractGatewayFilterFactory<JwtAuthFilter.Config> {

    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();
            String method = request.getMethodValue();

            List<String> ignorePaths = config.getIgnorePaths();
            if (ignorePaths != null && !ignorePaths.isEmpty()) {
                for (String raw : ignorePaths) {
                    for (String ignorePath : splitIgnorePaths(raw)) {
                        if (pathMatcher.match(ignorePath, path)) {
                            return chain.filter(exchange);
                        }
                    }
                }
            }

            String auth = request.getHeaders().getFirst("Authorization");
            if (!StringUtils.hasText(auth) || !auth.startsWith("Bearer ")) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
            String token = auth.substring(7).trim();

            Claims claims = jwtUtil.parseToken(token);
            if (claims == null || claims.getExpiration() != null && claims.getExpiration().before(new java.util.Date())) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            Object userIdObj = claims.get("userId");
            String username = claims.get("username", String.class);
            String roleCode = claims.get("roleCode", String.class);
            long userId = userIdObj instanceof Number ? ((Number) userIdObj).longValue() : 0L;
            if (userId <= 0 || !StringUtils.hasText(username)) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            // 访客权限限制：拒绝创建/修改/提交类接口；对通知接口仅保留只读与标记已读能力
            String role = StringUtils.hasText(roleCode) ? roleCode.trim().toUpperCase() : "VISITOR";
            if ("VISITOR".equals(role)) {
                // 通知相关接口单独处理：允许 GET 列表和 POST 标记已读，禁止删除与配置类写操作
                if (pathMatcher.match("/api/notification/**", path) || pathMatcher.match("/notification/**", path)
                        || pathMatcher.match("/api/notice/**", path) || pathMatcher.match("/notice/**", path)) {
                    // DELETE/PUT/PATCH 一律禁止
                    if ("DELETE".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method) || "PATCH".equalsIgnoreCase(method)) {
                        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                        return exchange.getResponse().setComplete();
                    }
                    // 允许 GET 与用于标记已读的 POST，其余 POST 写操作在通知服务内部再做精细校验
                } else {
                    // 非通知接口：拦截写操作（POST/PUT/DELETE/PATCH），仅允许 GET
                    if (!"GET".equalsIgnoreCase(method)) {
                        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                        return exchange.getResponse().setComplete();
                    }
                }
            }

            ServerHttpRequest mutated = request.mutate()
                    .header("X-User-Id", String.valueOf(userId))
                    .header("X-Username", username != null ? username : "")
                    .header("X-Role", role)
                    .build();
            return chain.filter(exchange.mutate().request(mutated).build());
        };
    }

    public static class Config {
        private List<String> ignorePaths;
        
        public List<String> getIgnorePaths() {
            return ignorePaths;
        }
        
        public void setIgnorePaths(List<String> ignorePaths) {
            this.ignorePaths = ignorePaths;
        }
    }

    /** 将配置中的一条（可能为逗号分隔）拆成多个 path，并 trim */
    private static List<String> splitIgnorePaths(String raw) {
        if (raw == null || raw.isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList("ignorePaths");
    }
}