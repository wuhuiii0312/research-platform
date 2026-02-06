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
     * 用户登录。管理员 admin 可任选角色；其他用户只能使用注册时的角色，否则返回「请选择正确的角色身份」
     * 返回 data 含 token、userId、username，供前端统一存储与个人中心展示
     */
    CommonResult<?> login(String username, String password, String role);

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
