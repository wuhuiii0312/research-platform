ALTER TABLE notification
    ADD COLUMN IF NOT EXISTS biz_type VARCHAR(50) COMMENT '业务维度：PROJECT/TASK/DOCUMENT/RESULT/SYSTEM' AFTER type,
    ADD COLUMN IF NOT EXISTS biz_id BIGINT COMMENT '业务主键ID，如 projectId/taskId/documentId/resultId' AFTER biz_type,
    ADD COLUMN IF NOT EXISTS project_id BIGINT COMMENT '所属项目ID，便于按项目筛选与权限判断' AFTER biz_id,
    ADD COLUMN IF NOT EXISTS priority VARCHAR(20) DEFAULT 'NORMAL' COMMENT '优先级：LOW/NORMAL/HIGH/CRITICAL' AFTER project_id,
    ADD COLUMN IF NOT EXISTS action_type VARCHAR(50) COMMENT '联动动作：APPROVAL/VIEW_DETAIL/OPEN_PUBLIC_PAGE 等' AFTER priority,
    ADD COLUMN IF NOT EXISTS extra TEXT COMMENT '扩展信息JSON，存审批类型、跳转路由等' AFTER action_type;

CREATE INDEX IF NOT EXISTS idx_notification_project ON notification (project_id);
CREATE INDEX IF NOT EXISTS idx_notification_biz ON notification (biz_type, biz_id);

