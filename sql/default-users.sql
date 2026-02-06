-- 默认登录用户（密码均为 admin123 的 BCrypt 加密值）
-- 执行前请先执行 schema.sql 创建 sys_user 表；若表中已有 admin/zhangsan 可跳过
USE research_platform;

INSERT IGNORE INTO sys_user (username, password, name, role_code, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'admin', 'admin', 1),
('zhangsan', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'zhangsan', 'user', 1);
