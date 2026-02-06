-- 为 task 表增加逻辑删除及审计字段，使「删除任务」在后端持久生效
-- 执行前若表已有某列，可跳过对应 ALTER 或注释掉该行

USE research_platform;

-- 逻辑删除与审计（必加，否则删除仅前端生效）
ALTER TABLE task ADD COLUMN del_flag INT DEFAULT 0 COMMENT '0-正常 1-删除';
ALTER TABLE task ADD COLUMN create_by BIGINT NULL;
ALTER TABLE task ADD COLUMN create_time DATETIME NULL;
ALTER TABLE task ADD COLUMN update_by BIGINT NULL;
ALTER TABLE task ADD COLUMN update_time DATETIME NULL;

-- 若无 idx_task_del_flag 可执行下一行
-- CREATE INDEX idx_task_del_flag ON task (del_flag);
