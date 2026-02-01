package com.research.auth.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.research.auth.config.JwtUtils;
import com.research.auth.entity.User;
import com.research.auth.mapper.UserMapper;
import com.research.auth.model.LoginUser;
import com.research.auth.service.AuthService;
import com.research.common.core.constant.Constants;
import com.research.common.core.domain.CommonResult;
import com.research.common.core.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现
 */
@Slf4j
@Service
public class AuthServiceImpl extends ServiceImpl<UserMapper, User> implements AuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Value("${jwt.expire:7200000}")
    private long jwtExpire;

    private static final String CAPTCHA_KEY = Constants.CAPTCHA_PREFIX;
    private static final String BLACKLIST_KEY = Constants.TOKEN_BLACKLIST_PREFIX;
    private static final int CAPTCHA_EXPIRE_MINUTES = 5;
    private static final int BLACKLIST_EXPIRE_HOURS = 24;

    @Override
    public CommonResult<String> login(String username, String password) {
        if (StrUtil.isBlank(username) || StrUtil.isBlank(password)) {
            return CommonResult.fail(HttpStatus.BAD_REQUEST.value(), "用户名和密码不能为空");
        }
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
                .eq(User::getStatus, 1));
        if (user == null) {
            return CommonResult.fail(HttpStatus.UNAUTHORIZED.value(), "用户名或密码错误");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return CommonResult.fail(HttpStatus.UNAUTHORIZED.value(), "用户名或密码错误");
        }
        String token = jwtUtils.generateToken(user.getId(), user.getUsername());
        log.info("用户登录成功: userId={}, username={}", user.getId(), username);
        return CommonResult.success(token);
    }

    @Override
    public CommonResult<?> logout(String token) {
        if (StrUtil.isNotBlank(token)) {
            String key = BLACKLIST_KEY + token;
            redisTemplate.opsForValue().set(key, "1", BLACKLIST_EXPIRE_HOURS, TimeUnit.HOURS);
        }
        return CommonResult.success(null);
    }

    @Override
    public CommonResult<LoginUser> getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return CommonResult.fail(HttpStatus.NOT_FOUND.value(), "用户不存在");
        }
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(user.getId());
        loginUser.setUsername(user.getUsername());
        loginUser.setName(user.getName());
        loginUser.setAvatar(user.getAvatar());
        loginUser.setRoles(user.getRoleCode() != null ? Collections.singletonList(user.getRoleCode()) : Collections.emptyList());
        loginUser.setPermissions(Collections.emptyList());
        return CommonResult.success(loginUser);
    }

    @Override
    public CommonResult<String> refreshToken(String refreshToken) {
        if (StrUtil.isBlank(refreshToken)) {
            return CommonResult.fail(HttpStatus.BAD_REQUEST.value(), "refreshToken不能为空");
        }
        try {
            Long userId = jwtUtils.getUserIdFromToken(refreshToken);
            User user = userMapper.selectById(userId);
            if (user == null) {
                return CommonResult.fail(HttpStatus.UNAUTHORIZED.value(), "用户不存在");
            }
            String newToken = jwtUtils.generateToken(user.getId(), user.getUsername());
            return CommonResult.success(newToken);
        } catch (Exception e) {
            log.warn("刷新Token失败: {}", e.getMessage());
            return CommonResult.fail(HttpStatus.UNAUTHORIZED.value(), "Token无效或已过期");
        }
    }

    @Override
    public CommonResult<?> getCaptcha() {
        String uuid = IdUtil.fastSimpleUUID();
        // 简单数字验证码 4 位
        String code = String.format("%04d", new Random().nextInt(10000));
        String key = CAPTCHA_KEY + uuid;
        redisTemplate.opsForValue().set(key, code, CAPTCHA_EXPIRE_MINUTES, TimeUnit.MINUTES);
        Map<String, Object> result = new HashMap<>();
        result.put("uuid", uuid);
        result.put("code", code);
        result.put("expire", CAPTCHA_EXPIRE_MINUTES * 60);
        return CommonResult.success(result);
    }

    @Override
    public CommonResult<?> register(User user) {
        if (user == null || StrUtil.isBlank(user.getUsername())) {
            throw new BusinessException("用户名不能为空");
        }
        if (StrUtil.isBlank(user.getPassword())) {
            throw new BusinessException("密码不能为空");
        }
        Long cnt = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, user.getUsername()));
        if (cnt > 0) {
            throw new BusinessException("用户名已存在");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setStatus(1);
        user.setRoleCode("user");
        if (user.getName() == null) {
            user.setName(user.getUsername());
        }
        userMapper.insert(user);
        log.info("用户注册成功: username={}", user.getUsername());
        return CommonResult.success("注册成功");
    }

    @Override
    public CommonResult<?> changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
        return CommonResult.success("修改成功");
    }

    @Override
    public CommonResult<?> resetPassword(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setPassword(passwordEncoder.encode("123456"));
        userMapper.updateById(user);
        return CommonResult.success("重置成功，默认密码：123456");
    }

    @Override
    public LoginUser getLoginUser() {
        return null;
    }
}
