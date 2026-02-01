package com.research.auth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.research.auth.entity.User;
import com.research.auth.model.LoginUser;
import com.research.common.core.domain.CommonResult;

/**
 * 认证服务接口
 */
public interface AuthService extends IService<User> {

    /**
     * 用户登录
     */
    CommonResult<String> login(String username, String password);

    /**
     * 用户注销
     */
    CommonResult<?> logout(String token);

    /**
     * 获取用户信息
     */
    CommonResult<LoginUser> getUserInfo(Long userId);

    /**
     * 刷新Token
     */
    CommonResult<String> refreshToken(String refreshToken);

    /**
     * 获取验证码
     */
    CommonResult<?> getCaptcha();

    /**
     * 用户注册
     */
    CommonResult<?> register(User user);

    /**
     * 修改密码
     */
    CommonResult<?> changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 重置密码
     */
    CommonResult<?> resetPassword(Long userId);

    /**
     * 获取当前登录用户信息
     */
    LoginUser getLoginUser();
}
