-- 科研项目协作平台 - 最终版建表脚本（解决1406/3730/1050所有报错）
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 彻底删除所有表（按依赖顺序）
DROP TABLE IF EXISTS task_attachment;
DROP TABLE IF EXISTS task_comment;
DROP TABLE IF EXISTS task;
DROP TABLE IF EXISTS document_meta;
DROP TABLE IF EXISTS project_result;
DROP TABLE IF EXISTS project_apply;
DROP TABLE IF EXISTS project_member;
DROP TABLE IF EXISTS project;
DROP TABLE IF EXISTS sys_user;

-- 创建数据库（强制指定utf8mb4）
CREATE DATABASE IF NOT EXISTS research_platform 
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_general_ci;
USE research_platform;

-- 用户表（与 research-auth User 实体字段一致）
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(200) NOT NULL,
    name VARCHAR(200) COMMENT '真实姓名',
    role_code VARCHAR(50) DEFAULT 'user',
    email VARCHAR(100),
    phone VARCHAR(20),
    avatar VARCHAR(255),
    status INT DEFAULT 1,
    last_login_ip VARCHAR(50),
    last_login_time DATETIME,
    dept_id BIGINT,
    post_id BIGINT,
    gender INT,
    profile VARCHAR(500),
    create_by BIGINT,
    create_time DATETIME,
    update_by BIGINT,
    update_time DATETIME,
    del_flag INT DEFAULT 0,
    remark VARCHAR(500),
    invite_code VARCHAR(5) COMMENT '五位数字邀请码，供负责人邀请入项目',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_invite_code (invite_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 项目表（需求文档：name, description, leader_id, status, start_time, end_time + project_no + BaseEntity）
CREATE TABLE IF NOT EXISTS project (
    id BIGINT NOT NULL AUTO_INCREMENT,
    project_no INT NULL COMMENT '5位项目编号10000-99999唯一',
    name VARCHAR(200) NOT NULL,
    description TEXT,
    leader_id BIGINT NOT NULL,
    status VARCHAR(20) DEFAULT 'INIT',
    start_time DATE,
    end_time DATE,
    progress INT DEFAULT 0,
    is_public TINYINT DEFAULT 0 COMMENT '0-不公开仅成员可见 1-对外公开所有人可浏览',
    del_flag INT DEFAULT 0,
    create_by BIGINT,
    create_time DATETIME,
    update_by BIGINT,
    update_time DATETIME,
    remark VARCHAR(500),
    PRIMARY KEY (id),
    UNIQUE KEY uk_project_no (project_no),
    KEY idx_leader (leader_id),
    KEY idx_del_flag (del_flag),
    CONSTRAINT fk_project_leader FOREIGN KEY (leader_id) REFERENCES sys_user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 任务表（含逻辑删除与审计字段，保证删除操作可持久化）
CREATE TABLE IF NOT EXISTS task (
    id BIGINT NOT NULL AUTO_INCREMENT,
    project_id BIGINT NOT NULL,
    parent_id BIGINT NULL COMMENT '父任务ID，空为顶级任务',
    name VARCHAR(200) NOT NULL,
    description TEXT,
    assignee_id BIGINT,
    reporter_id BIGINT,
    status VARCHAR(20) DEFAULT 'TODO',
    priority VARCHAR(20) DEFAULT 'MEDIUM',
    type VARCHAR(50),
    estimated_hours DECIMAL(10,2),
    actual_hours DECIMAL(10,2),
    start_time DATE,
    end_time DATE,
    due_time DATE,
    progress INT DEFAULT 0,
    tags VARCHAR(500),
    attachment_count INT DEFAULT 0,
    comment_count INT DEFAULT 0,
    create_by BIGINT,
    create_time DATETIME,
    update_by BIGINT,
    update_time DATETIME,
    del_flag INT DEFAULT 0 COMMENT '0-正常 1-删除',
    remark VARCHAR(500),
    PRIMARY KEY (id),
    KEY idx_project (project_id),
    KEY idx_del_flag (del_flag),
    CONSTRAINT fk_task_project FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 项目成员表（多角色：LEADER/MEMBER/VISITOR，贴合开题报告）
CREATE TABLE IF NOT EXISTS project_member (
    id BIGINT NOT NULL AUTO_INCREMENT,
    project_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'MEMBER' COMMENT '项目内角色：LEADER/MEMBER/VISITOR',
    status INT NOT NULL DEFAULT 1 COMMENT '1=已加入 0=待审批',
    join_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_project_user (project_id, user_id),
    KEY idx_user (user_id),
    CONSTRAINT fk_project_member_project FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE,
    CONSTRAINT fk_project_member_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 项目申请加入表（主动申请-负责人审批）
CREATE TABLE IF NOT EXISTS project_apply (
    id BIGINT NOT NULL AUTO_INCREMENT,
    project_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED',
    apply_reason VARCHAR(500),
    reply_remark VARCHAR(500),
    reply_by BIGINT,
    reply_time DATETIME,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_project (project_id),
    KEY idx_user (user_id),
    CONSTRAINT fk_apply_project FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE,
    CONSTRAINT fk_apply_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 科研项目成果表（成果归档、审核流程、逻辑删除）
CREATE TABLE IF NOT EXISTS project_result (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '成果ID',
    project_id BIGINT NOT NULL COMMENT '所属项目ID',
    name VARCHAR(200) NOT NULL COMMENT '成果名称',
    type VARCHAR(20) NOT NULL COMMENT 'PAPER-论文/PATENT-专利/SOFT-软著/REPORT-报告',
    description TEXT COMMENT '成果描述',
    result_content TEXT COMMENT '成果正文内容',
    related_document_ids VARCHAR(1000) COMMENT '关联文档ID列表，逗号分隔',
    file_url VARCHAR(500) COMMENT '附件地址',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT 'PENDING-待审核/PASSED-已通过/ARCHIVED-已归档',
    submitter_id BIGINT NOT NULL COMMENT '提交人ID',
    submit_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    audit_user_id BIGINT COMMENT '审核人ID',
    audit_time DATETIME COMMENT '审核时间',
    audit_remark VARCHAR(500) COMMENT '审核备注',
    del_flag INT DEFAULT 0 COMMENT '0-正常/1-删除',
    test_data_flag TINYINT DEFAULT 0 COMMENT '是否测试数据（0-否/1-是）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_project (project_id),
    KEY idx_submitter (submitter_id),
    KEY idx_status (status),
    KEY idx_test_data_flag (test_data_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '科研项目成果表';

-- 文档元数据表（MongoDB+ES 配合，归属标识：project_id/creator_id/permission_type）
CREATE TABLE IF NOT EXISTS document_meta (
    id BIGINT NOT NULL AUTO_INCREMENT,
    project_id BIGINT NOT NULL COMMENT '所属项目',
    creator_id BIGINT NOT NULL COMMENT '创建者user_id',
    permission_type VARCHAR(20) NOT NULL DEFAULT 'MEMBER' COMMENT 'LEADER/MEMBER/VISITOR 可见',
    is_public TINYINT DEFAULT 0 COMMENT '0-私有 1-公开',
    name VARCHAR(200) NOT NULL,
    file_path VARCHAR(500),
    file_size BIGINT DEFAULT 0,
    file_type VARCHAR(50),
    version INT DEFAULT 1,
    mongo_id VARCHAR(64),
    del_flag INT DEFAULT 0,
    test_data_flag TINYINT DEFAULT 0 COMMENT '是否测试数据（0-否/1-是）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_project (project_id),
    KEY idx_creator (creator_id),
    KEY idx_doc_test_data_flag (test_data_flag),
    CONSTRAINT fk_doc_project FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE,
    CONSTRAINT fk_doc_creator FOREIGN KEY (creator_id) REFERENCES sys_user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS task_attachment (
    id BIGINT NOT NULL AUTO_INCREMENT,
    task_id BIGINT NOT NULL,
    file_name VARCHAR(200) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_task (task_id),
    CONSTRAINT fk_attachment_task FOREIGN KEY (task_id) REFERENCES task(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 系统通知表（通知中心）
CREATE TABLE IF NOT EXISTS notification (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '通知ID',
    user_id BIGINT NOT NULL COMMENT '接收用户ID',
    type VARCHAR(50) COMMENT '旧版类型：task/document/system',
    biz_type VARCHAR(50) COMMENT '业务维度：PROJECT/TASK/DOCUMENT/RESULT/SYSTEM',
    biz_id BIGINT COMMENT '业务主键ID，如 projectId/taskId/documentId/resultId',
    project_id BIGINT COMMENT '所属项目ID，便于按项目筛选与权限判断',
    priority VARCHAR(20) DEFAULT 'NORMAL' COMMENT '优先级：LOW/NORMAL/HIGH/CRITICAL',
    action_type VARCHAR(50) COMMENT '联动动作：APPROVAL/VIEW_DETAIL/OPEN_PUBLIC_PAGE 等',
    extra TEXT COMMENT '扩展信息JSON，存审批类型、跳转路由等',
    title VARCHAR(200) NOT NULL COMMENT '通知标题',
    content TEXT COMMENT '通知内容',
    read_flag INT DEFAULT 0 COMMENT '0-未读/1-已读',
    send_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    del_flag INT DEFAULT 0 COMMENT '0-正常/1-删除',
    PRIMARY KEY (id),
    KEY idx_user (user_id),
    KEY idx_read (read_flag),
    KEY idx_project (project_id),
    KEY idx_biz (biz_type, biz_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '系统通知表';

-- 系统操作日志表（个人中心操作记录）
CREATE TABLE IF NOT EXISTS sys_operation_log (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    user_id BIGINT COMMENT '操作用户ID',
    title VARCHAR(100) COMMENT '操作标题',
    type VARCHAR(50) COMMENT '操作类型',
    method VARCHAR(200) COMMENT '请求方法',
    url VARCHAR(255) COMMENT '请求URL',
    ip VARCHAR(50) COMMENT '客户端IP',
    status INT COMMENT '0-失败/1-成功',
    error_msg VARCHAR(500) COMMENT '错误信息',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (id),
    KEY idx_user (user_id),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '系统操作日志表';

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

-- 插入测试账号（密码均为 123456，BCrypt 加密；invite_code 五位数字供负责人邀请）
INSERT INTO sys_user (username, password, name, role_code, status, invite_code) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '管理员', 'admin', 1, '10001'),
('leader', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '项目负责人', 'LEADER', 1, '10002'),
('member', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '科研人员', 'MEMBER', 1, '10003'),
('visitor', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '访客', 'VISITOR', 1, '10004'),
('zhangsan', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '张三', 'user', 1, '10005')
ON DUPLICATE KEY UPDATE name = VALUES(name), role_code = VALUES(role_code);