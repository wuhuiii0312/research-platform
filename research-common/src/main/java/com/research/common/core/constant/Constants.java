package com.research.common.core.constant;

/**
 * 通用常量
 */
public class Constants {
    /** 正常状态 */
    public static final Integer NORMAL = 0;
    /** 停用状态 */
    public static final Integer DISABLE = 1;
    /** 删除标志-未删除 */
    public static final Integer DEL_FLAG_NORMAL = 0;
    /** 删除标志-已删除 */
    public static final Integer DEL_FLAG_DELETED = 1;
    /** 管理员角色编码 */
    public static final String ROLE_ADMIN = "admin";
    /** JWT Token 请求头 */
    public static final String TOKEN_HEADER = "Authorization";
    /** JWT Token 前缀 */
    public static final String TOKEN_PREFIX = "Bearer ";
    /** 验证码 Redis 前缀 */
    public static final String CAPTCHA_PREFIX = "captcha:";
    /** 黑名单 Redis 前缀 */
    public static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";
}
