-- 为已存在的 sys_user 表添加缺失列（若表是按旧版 schema 创建的，执行一次即可）
USE research_platform;

ALTER TABLE sys_user ADD COLUMN email VARCHAR(100);
ALTER TABLE sys_user ADD COLUMN phone VARCHAR(20);
ALTER TABLE sys_user ADD COLUMN avatar VARCHAR(255);
ALTER TABLE sys_user ADD COLUMN last_login_ip VARCHAR(50);
ALTER TABLE sys_user ADD COLUMN last_login_time DATETIME;
ALTER TABLE sys_user ADD COLUMN dept_id BIGINT;
ALTER TABLE sys_user ADD COLUMN post_id BIGINT;
ALTER TABLE sys_user ADD COLUMN gender INT;
ALTER TABLE sys_user ADD COLUMN profile VARCHAR(500);
ALTER TABLE sys_user ADD COLUMN create_by BIGINT;
ALTER TABLE sys_user ADD COLUMN create_time DATETIME;
ALTER TABLE sys_user ADD COLUMN update_by BIGINT;
ALTER TABLE sys_user ADD COLUMN update_time DATETIME;
ALTER TABLE sys_user ADD COLUMN del_flag INT DEFAULT 0;
ALTER TABLE sys_user ADD COLUMN remark VARCHAR(500);
