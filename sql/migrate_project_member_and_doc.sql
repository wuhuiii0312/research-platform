-- 为 project_member 增加 role/status，并新增 project_apply、document_meta（已有库执行一次）
USE research_platform;

-- project_member 增加列（若已存在则跳过，需手动检查）
ALTER TABLE project_member ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'MEMBER' COMMENT 'LEADER/MEMBER/VISITOR';
ALTER TABLE project_member ADD COLUMN status INT NOT NULL DEFAULT 1 COMMENT '1=已加入 0=待审批';
ALTER TABLE project_member ADD COLUMN join_time DATETIME DEFAULT CURRENT_TIMESTAMP;

-- project_apply 表（申请加入项目）
CREATE TABLE IF NOT EXISTS project_apply (
    id BIGINT NOT NULL AUTO_INCREMENT,
    project_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
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

-- document_meta 表（文档元数据：project_id/creator_id/permission_type）
CREATE TABLE IF NOT EXISTS document_meta (
    id BIGINT NOT NULL AUTO_INCREMENT,
    project_id BIGINT NOT NULL,
    creator_id BIGINT NOT NULL,
    permission_type VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
    name VARCHAR(200) NOT NULL,
    file_path VARCHAR(500),
    file_size BIGINT DEFAULT 0,
    file_type VARCHAR(50),
    version INT DEFAULT 1,
    mongo_id VARCHAR(64),
    del_flag INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_project (project_id),
    KEY idx_creator (creator_id),
    CONSTRAINT fk_doc_project FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE,
    CONSTRAINT fk_doc_creator FOREIGN KEY (creator_id) REFERENCES sys_user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
