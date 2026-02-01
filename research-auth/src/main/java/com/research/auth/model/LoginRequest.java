package com.research.auth.model;

import lombok.Data;

/**
 * 登录请求参数
 */
@Data
public class LoginRequest {
    private String username;
    private String password;
    private String captcha;
    private String uuid;
}
