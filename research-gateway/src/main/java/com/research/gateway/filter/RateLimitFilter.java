package com.research.gateway.filter;

import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 请求限流过滤器（修复令牌桶补充逻辑）
 */
@Slf4j
@Component
public class RateLimitFilter extends AbstractGatewayFilterFactory<RateLimitFilter.Config> {

    private static final ConcurrentHashMap<String, RateLimiter> RATE_LIMITER_MAP = new ConcurrentHashMap<>();

    public RateLimitFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String clientIp = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
            String key = "rate_limit:" + clientIp;

            // 本地开发环境放行（避免本机调试频繁触发限流）
            if ("127.0.0.1".equals(clientIp) || "0:0:0:0:0:0:0:1".equals(clientIp)) {
                return chain.filter(exchange);
            }

            // 根据配置创建令牌桶（每秒补充refillTokens个令牌，容量capacity）
            RateLimiter rateLimiter = RATE_LIMITER_MAP.computeIfAbsent(key,
                    k -> RateLimiter.create(config.getRefillTokens(), config.getRefillDuration(), TimeUnit.SECONDS));

            // 尝试获取令牌
            if (rateLimiter.tryAcquire()) {
                return chain.filter(exchange);
            } else {
                log.warn("客户端 {} 请求过于频繁，已被限流", clientIp);
                return tooManyRequests(exchange.getResponse());
            }
        };
    }

    /**
     * 返回请求过多响应
     */
    private Mono<Void> tooManyRequests(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String message = "{\"code\": 429, \"msg\": \"请求过于频繁，请稍后重试\"}";
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);

        return response.writeWith(Mono.just(buffer));
    }

    /**
     * 过滤器配置类
     */
    public static class Config {
        /**
         * 令牌桶容量
         */
        private double capacity = 100.0;

        /**
         * 每秒补充的令牌数
         */
        private double refillTokens = 10.0;

        /**
         * 补充令牌的时间间隔（秒）
         */
        private long refillDuration = 1;

        public double getCapacity() {
            return capacity;
        }

        public void setCapacity(double capacity) {
            this.capacity = capacity;
        }

        public double getRefillTokens() {
            return refillTokens;
        }

        public void setRefillTokens(double refillTokens) {
            this.refillTokens = refillTokens;
        }

        public long getRefillDuration() {
            return refillDuration;
        }

        public void setRefillDuration(long refillDuration) {
            this.refillDuration = refillDuration;
        }
    }
}