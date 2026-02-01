-- 科研项目协作平台 - 最终版建表脚本（解决1406/3730/1050所有报错）
SET FOREIGN_KEY_CHECKS = 0;

-- 彻底删除所有表（按依赖顺序）
DROP TABLE IF EXISTS task_attachment;
DROP TABLE IF EXISTS task_comment;
DROP TABLE IF EXISTS task;
DROP TABLE IF EXISTS project_member;
DROP TABLE IF EXISTS project;
DROP TABLE IF EXISTS sys_user;

-- 创建数据库（强制指定utf8mb4）
CREATE DATABASE IF NOT EXISTS research_platform 
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_general_ci;
USE research_platform;

-- 用户表（name列长度100，彻底避免1406错误）
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(200) NOT NULL,
    name VARCHAR(100) COMMENT '真实姓名',
    role_code VARCHAR(50) DEFAULT 'user',
    status INT DEFAULT 1,
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 项目表
CREATE TABLE IF NOT EXISTS project (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(200) NOT NULL,
    leader_id BIGINT NOT NULL,
    status VARCHAR(20) DEFAULT 'INIT',
    PRIMARY KEY (id),
    KEY idx_leader (leader_id),
    CONSTRAINT fk_project_leader FOREIGN KEY (leader_id) REFERENCES sys_user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 任务表
CREATE TABLE IF NOT EXISTS task (
    id BIGINT NOT NULL AUTO_INCREMENT,
    project_id BIGINT NOT NULL,
    name VARCHAR(200) NOT NULL,
    status VARCHAR(20) DEFAULT 'TODO',
    PRIMARY KEY (id),
    KEY idx_project (project_id),
    CONSTRAINT fk_task_project FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 其他表（简化，保留核心结构）
CREATE TABLE IF NOT EXISTS project_member (
    id BIGINT NOT NULL AUTO_INCREMENT,
    project_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_project_user (project_id, user_id),
    CONSTRAINT fk_project_member_project FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE,
    CONSTRAINT fk_project_member_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS task_attachment (
    id BIGINT NOT NULL AUTO_INCREMENT,
    task_id BIGINT NOT NULL,
    file_name VARCHAR(200) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_task (task_id),
    CONSTRAINT fk_attachment_task FOREIGN KEY (task_id) REFERENCES task(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS task_comment (
    id BIGINT NOT NULL AUTO_INCREMENT,
    task_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    KEY idx_task (task_id),
    CONSTRAINT fk_comment_task FOREIGN KEY (task_id) REFERENCES task(id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET FOREIGN_KEY_CHECKS = 1;

-- 插入极简数据（无长度问题）
INSERT INTO sys_user (username, password, name, role_code, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'admin', 'admin', 1),
('zhangsan', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'zhangsan', 'user', 1);