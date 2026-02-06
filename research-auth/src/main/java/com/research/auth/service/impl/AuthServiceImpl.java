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

    @Value("${jwt.expire:7200}")
    private long jwtExpire;

    private static final String CAPTCHA_KEY = Constants.CAPTCHA_PREFIX;
    private static final String BLACKLIST_KEY = Constants.TOKEN_BLACKLIST_PREFIX;
    private static final int CAPTCHA_EXPIRE_MINUTES = 5;
    private static final int BLACKLIST_EXPIRE_HOURS = 24;

    // 关键修改：将返回值从 CommonResult<String> 改为 CommonResult<Map<String, Object>>
    @Override
    public CommonResult<Map<String, Object>> login(String username, String password, String role) {
        // 1) 参数校验
        if (StrUtil.isBlank(username)) {
            return CommonResult.fail(HttpStatus.BAD_REQUEST.value(), "用户名不能为空");
        }
        if (StrUtil.isBlank(password)) {
            return CommonResult.fail(HttpStatus.BAD_REQUEST.value(), "密码不能为空");
        }
        if (StrUtil.isBlank(role)) {
            // 前端是“选择身份 + 用户名密码”，这里给出明确提示
            return CommonResult.fail(HttpStatus.BAD_REQUEST.value(), "请选择登录身份");
        }

        // 2) 用户校验：先按用户名查，再区分「不存在/被禁用」
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
        if (user == null) {
            return CommonResult.fail(HttpStatus.UNAUTHORIZED.value(), "用户名不存在");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            return CommonResult.fail(HttpStatus.FORBIDDEN.value(), "账号已被禁用，请联系管理员");
        }

        // 3) 密码校验
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return CommonResult.fail(HttpStatus.UNAUTHORIZED.value(), "密码错误");
        }

        // 非管理员只能使用注册时的角色登录，否则提示「请选择正确的角色身份」
        if (!Constants.ROLE_ADMIN.equalsIgnoreCase(user.getUsername())) {
            String userRole = user.getRoleCode() != null ? user.getRoleCode().trim() : "";
            String selectedRole = role != null ? role.trim() : "";
            if (!selectedRole.equalsIgnoreCase(userRole)) {
                return CommonResult.fail(HttpStatus.FORBIDDEN.value(), "请选择正确的角色身份");
            }
        }
        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRoleCode());
        log.info("用户登录成功: userId={}, username={}, role={}", user.getId(), username, role);
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.getId());
        data.put("username", user.getUsername());
        return CommonResult.success(data); // 现在类型匹配了
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
        loginUser.setInviteCode(user.getInviteCode());
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
            String newToken = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRoleCode());
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
        // 保留前端传入的角色：LEADER/MEMBER/VISITOR，不再覆盖为 user
        if (user.getRoleCode() == null || user.getRoleCode().isEmpty()) {
            user.setRoleCode("MEMBER");
        }
        if (user.getName() == null) {
            user.setName(user.getUsername());
        }
        // 五位数字邀请码（10000-99999），供项目负责人邀请入项目
        String inviteCode = generateUniqueInviteCode();
        user.setInviteCode(inviteCode);
        userMapper.insert(user);
        log.info("用户注册成功: username={}, inviteCode={}", user.getUsername(), inviteCode);
        Map<String, Object> data = new HashMap<>();
        data.put("message", "注册成功");
        data.put("inviteCode", inviteCode);
        return CommonResult.success(data);
    }

    /** 生成唯一五位数字邀请码 */
    private String generateUniqueInviteCode() {
        Random r = new Random();
        for (int i = 0; i < 50; i++) {
            int n = 10000 + r.nextInt(90000);
            String code = String.valueOf(n);
            Long cnt = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getInviteCode, code));
            if (cnt == null || cnt == 0) return code;
        }
        return String.valueOf(10000 + r.nextInt(90000));
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