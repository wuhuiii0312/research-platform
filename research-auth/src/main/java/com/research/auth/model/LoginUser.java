package com.research.auth.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 登录用户信息
 */
@Data
public class LoginUser {
    private Long userId;
    private String username;
    private String name;
    private String avatar;
    private List<String> roles;
    private List<String> permissions;
    private String token;
    private LocalDateTime loginTime;
    private LocalDateTime expireTime;
}
