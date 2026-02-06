package com.research.auth.controller;

import com.research.auth.entity.User;
import com.research.auth.model.LoginRequest;
import com.research.auth.service.AuthService;
import com.research.common.core.domain.CommonResult;
import com.research.common.core.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

/**
 * 认证接口：根路径映射，与网关 StripPrefix=1 对应（网关 /auth/login → 此处 /login）
 */
@RestController
@RequestMapping("/")
public class LoginController {

    @Autowired
    private AuthService authService;

    /** 健康检查：网关 http://localhost:9527/auth/health → 此处 /health */
    @GetMapping("/health")
    public Map<String, String> health() {
        return Collections.singletonMap("status", "UP");
    }

    /** 测试接口：网关访问 http://网关IP:9527/auth/test 即可触发 */
    @GetMapping("/test")
    public String authTest() {
        return "research-auth服务正常响应！";
    }

    @PostMapping("/login")
    public CommonResult<?> login(@RequestBody LoginRequest loginRequest) {
        if (loginRequest == null) {
            return CommonResult.fail(400, "请求参数不能为空");
        }
        return authService.login(loginRequest.getUsername(), loginRequest.getPassword(), loginRequest.getRole());
    }

    /** 获取当前登录用户信息（前端 /api/auth/getInfo，网关 StripPrefix=2 后为 GET /getInfo） */
    @GetMapping("/getInfo")
    public CommonResult<?> getInfo() {
        long userId = SecurityUtils.getUserId();
        if (userId <= 0) {
            return CommonResult.fail(401, "未登录或登录已过期");
        }
        return authService.getUserInfo(userId);
    }

    /** 注册接口（开题报告：统一密码123456，角色LEADER/MEMBER/VISITOR） */
    @PostMapping("/register")
    public CommonResult<?> register(@RequestBody RegisterRequest req) {
        if (req == null || req.getUsername() == null || req.getUsername().trim().isEmpty()) {
            return CommonResult.fail(400, "用户名不能为空");
        }
        User user = new User();
        user.setUsername(req.getUsername().trim());
        user.setPassword(req.getPassword() != null ? req.getPassword() : "123456");
        user.setRoleCode(req.getRole() != null ? req.getRole() : "MEMBER");
        user.setEmail(req.getEmail());
        user.setName(user.getUsername());
        return authService.register(user);
    }

    /** 注册请求体 */
    public static class RegisterRequest {
        private String username;
        private String password;
        private String role;
        private String email;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
}
