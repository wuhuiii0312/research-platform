package com.research.common.core.filter;

import com.research.common.core.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * 从网关下发的请求头中读取用户信息并设置到 SecurityUtils，供 /project/my 等接口使用。
 * 网关在 JwtAuthFilter 中解析 JWT 后设置 X-User-Id、X-Username。
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class UserContextFilter implements Filter {

    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_USERNAME = "X-Username";
    public static final String HEADER_ROLE = "X-Role";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest)) {
            chain.doFilter(request, response);
            return;
        }
        HttpServletRequest req = (HttpServletRequest) request;
        try {
            String userIdStr = req.getHeader(HEADER_USER_ID);
            String username = req.getHeader(HEADER_USERNAME);
            String roleCode = req.getHeader(HEADER_ROLE);
            if (StringUtils.hasText(userIdStr)) {
                try {
                    long userId = Long.parseLong(userIdStr.trim());
                    SecurityUtils.setUserId(userId);
                } catch (NumberFormatException e) {
                    log.debug("Invalid X-User-Id: {}", userIdStr);
                }
            }
            if (StringUtils.hasText(username)) {
                SecurityUtils.setUsername(username.trim());
            }
            if (StringUtils.hasText(roleCode)) {
                SecurityUtils.setRoleCode(roleCode.trim());
            }
            chain.doFilter(request, response);
        } finally {
            SecurityUtils.clear();
        }
    }
}
