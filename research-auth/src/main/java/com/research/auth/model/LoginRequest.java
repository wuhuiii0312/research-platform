package com.research.auth.model;

import lombok.Data;

/**
 * 登录请求参数
 */
@Data
public class LoginRequest {
    private String username;
    private String password;
    /** 登录时选择的角色：LEADER/MEMBER/VISITOR。管理员 admin 可任选；其他用户必须与注册角色一致 */
    private String role;
    private String captcha;
    private String uuid;
}
