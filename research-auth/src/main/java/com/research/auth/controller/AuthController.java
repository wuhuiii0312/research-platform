package com.research.auth.controller;

import com.research.auth.entity.User;
import com.research.auth.model.LoginRequest;
import com.research.auth.service.AuthService;
import com.research.common.core.annotation.Log;
import com.research.common.core.domain.CommonResult;
import com.research.common.core.enums.BusinessType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 认证控制器
 */
@RestController
@RequestMapping({"/auth", "/user"})  // 网关 /api/auth 与 /api/user 均转发到此服务
@Api(tags = "认证管理")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Log(title = "用户登录", businessType = BusinessType.LOGIN)
    @PostMapping("/login")
    @ApiOperation("用户登录")
    public CommonResult<String> login(@RequestBody LoginRequest loginRequest) {
        // 这里可以添加验证码验证逻辑
        return authService.login(loginRequest.getUsername(), loginRequest.getPassword());
    }

    @PostMapping("/logout")
    @ApiOperation("用户注销")
    public CommonResult<?> logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return authService.logout(token);
    }

    @GetMapping("/info")
    @ApiOperation("获取用户信息")
    public CommonResult<?> getUserInfo(@RequestParam Long userId) {
        return authService.getUserInfo(userId);
    }

    @PostMapping("/refresh")
    @ApiOperation("刷新Token")
    public CommonResult<String> refreshToken(@RequestParam String refreshToken) {
        return authService.refreshToken(refreshToken);
    }

    @GetMapping("/captcha")
    @ApiOperation("获取验证码")
    public CommonResult<?> getCaptcha() {
        return authService.getCaptcha();
    }

    @Log(title = "用户注册", businessType = BusinessType.INSERT)
    @PostMapping("/register")
    @ApiOperation("用户注册")
    public CommonResult<?> register(@RequestBody User user) {
        return authService.register(user);
    }

    @Log(title = "修改密码", businessType = BusinessType.UPDATE)
    @PostMapping("/change-password")
    @ApiOperation("修改密码")
    public CommonResult<?> changePassword(
            @RequestParam Long userId,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        return authService.changePassword(userId, oldPassword, newPassword);
    }

    @Log(title = "重置密码", businessType = BusinessType.UPDATE)
    @PostMapping("/reset-password")
    @ApiOperation("重置密码")
    public CommonResult<?> resetPassword(@RequestParam Long userId) {
        return authService.resetPassword(userId);
    }
}
