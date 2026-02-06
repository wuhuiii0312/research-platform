package com.research.auth.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.research.auth.entity.User;
import com.research.auth.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 启动时初始化测试账号（密码 123456，BCrypt 加密），确保登录可用
 */
@Slf4j
@Component
@Order(1)
public class DataInitializer implements ApplicationRunner {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String TEST_PASSWORD = "123456";

    @Override
    public void run(ApplicationArguments args) {
        String encoded = passwordEncoder.encode(TEST_PASSWORD);
        ensureUser("leader", "项目负责人", "LEADER", encoded);
        ensureUser("member", "科研人员", "MEMBER", encoded);
        ensureUser("visitor", "访客", "VISITOR", encoded);
        ensureUser("admin", "管理员", "admin", encoded);
    }

    private void ensureUser(String username, String name, String roleCode, String encodedPassword) {
        User u = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (u == null) {
            User user = new User();
            user.setUsername(username);
            user.setPassword(encodedPassword);
            user.setName(name);
            user.setRoleCode(roleCode);
            user.setStatus(1);
            userMapper.insert(user);
            log.info("初始化测试账号: {} ({})", username, name);
        } else {
            u.setPassword(encodedPassword);
            u.setName(name);
            u.setRoleCode(roleCode);
            u.setStatus(1);
            userMapper.updateById(u);
            log.debug("已修正测试账号密码: {}", username);
        }
    }
}
