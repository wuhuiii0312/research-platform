package com.research.common.core.util;

/**
 * 安全工具类（获取当前用户ID、用户名等）
 * 从 ThreadLocal 或 JWT 解析获取，微服务中可从请求头传递
 */
public class SecurityUtils {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> USERNAME = new ThreadLocal<>();
    private static final ThreadLocal<String> ROLE_CODE = new ThreadLocal<>();
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

    public static String getRoleCode() {
        return ROLE_CODE.get();
    }

    public static void setRoleCode(String roleCode) {
        ROLE_CODE.set(roleCode);
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
        ROLE_CODE.remove();
        ADMIN_FLAG.remove();
    }

    /**
     * 当前用户是否为全局访客（VISITOR）
     */
    public static boolean isGlobalVisitor() {
        String role = getRoleCode();
        return role != null && "VISITOR".equalsIgnoreCase(role.trim());
    }

    /**
     * 当前用户是否拥有指定角色（忽略大小写）
     */
    public static boolean hasRole(String roleCode) {
        String role = getRoleCode();
        return role != null && roleCode != null
                && role.trim().equalsIgnoreCase(roleCode.trim());
    }

    /**
     * 当前用户是否拥有任一给定角色
     */
    public static boolean hasAnyRole(String... roleCodes) {
        if (roleCodes == null || roleCodes.length == 0) {
            return false;
        }
        for (String code : roleCodes) {
            if (hasRole(code)) {
                return true;
            }
        }
        return false;
    }
}
