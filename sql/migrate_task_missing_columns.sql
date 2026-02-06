-- 为 task 表补全实体所需列（解决 Unknown column 'start_time' 等报错）
-- 若某列已存在会报 Duplicate column，跳过该句继续执行其余即可
USE research_platform;

-- 子任务关联
ALTER TABLE task ADD COLUMN parent_id BIGINT NULL COMMENT '父任务ID';

-- 业务字段（按需执行，已存在则跳过）
ALTER TABLE task ADD COLUMN description TEXT NULL;
ALTER TABLE task ADD COLUMN assignee_id BIGINT NULL;
ALTER TABLE task ADD COLUMN reporter_id BIGINT NULL;
ALTER TABLE task ADD COLUMN priority VARCHAR(20) DEFAULT 'MEDIUM';
ALTER TABLE task ADD COLUMN type VARCHAR(50) NULL;
ALTER TABLE task ADD COLUMN estimated_hours DECIMAL(10,2) NULL;
ALTER TABLE task ADD COLUMN actual_hours DECIMAL(10,2) NULL;
ALTER TABLE task ADD COLUMN start_time DATE NULL;
ALTER TABLE task ADD COLUMN end_time DATE NULL;
ALTER TABLE task ADD COLUMN due_time DATE NULL;
ALTER TABLE task ADD COLUMN progress INT DEFAULT 0;
ALTER TABLE task ADD COLUMN tags VARCHAR(500) NULL;
ALTER TABLE task ADD COLUMN attachment_count INT DEFAULT 0;
ALTER TABLE task ADD COLUMN comment_count INT DEFAULT 0;
ALTER TABLE task ADD COLUMN remark VARCHAR(500) NULL;

-- 逻辑删除与审计（若 migrate_task_soft_delete 已执行则跳过）
ALTER TABLE task ADD COLUMN create_by BIGINT NULL;
ALTER TABLE task ADD COLUMN create_time DATETIME NULL;
ALTER TABLE task ADD COLUMN update_by BIGINT NULL;
ALTER TABLE task ADD COLUMN update_time DATETIME NULL;
ALTER TABLE task ADD COLUMN del_flag INT DEFAULT 0 COMMENT '0-正常 1-删除';

-- 索引（若已存在可忽略）
-- CREATE INDEX idx_task_del_flag ON task (del_flag);
