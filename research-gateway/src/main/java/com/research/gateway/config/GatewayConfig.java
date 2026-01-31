package com.research.gateway.config;

import org.springframework.context.annotation.Configuration;

/**
 * 网关配置类（过滤器由配置文件绑定，无需硬编码路由）
 */
@Configuration
public class GatewayConfig {
    // 移除原有的 RouteLocator 配置，路由规则统一在 bootstrap.yml 中配置
}