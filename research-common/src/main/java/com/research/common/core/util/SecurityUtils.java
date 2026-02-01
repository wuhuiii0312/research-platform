package com.research.common.core.util;

import com.research.common.core.constant.Constants;

/**
 * 安全工具类（获取当前用户ID、用户名等）
 * 从 ThreadLocal 或 JWT 解析获取，微服务中可从请求头传递
 */
public class SecurityUtils {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> USERNAME = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> ADMIN_FLAG = new ThreadLocal<>();

    public static Long getUserId() {
        Long id = USER_ID.get();
        return id != null ? id : 1L;
    }

    public static void setUserId(Long userId) {
        USER_ID.set(userId);
    }

    public static String getUsername() {
        String name = USERNAME.get();
        return name != null ? name : "system";
    }

    public static void setUsername(String username) {
        USERNAME.set(username);
    }

    public static boolean isAdmin() {
        Boolean flag = ADMIN_FLAG.get();
        return Boolean.TRUE.equals(flag);
    }

    public static void setAdmin(boolean admin) {
        ADMIN_FLAG.set(admin);
    }

    public static void clear() {
        USER_ID.remove();
        USERNAME.remove();
        ADMIN_FLAG.remove();
    }
}
