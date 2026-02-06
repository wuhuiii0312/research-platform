-- 项目立项审核字段：审核意见、审核时间、审核人
SET NAMES utf8mb4;

ALTER TABLE project ADD COLUMN audit_opinion VARCHAR(1000) COMMENT '立项审核意见';
ALTER TABLE project ADD COLUMN audit_time DATETIME COMMENT '立项审核时间';
ALTER TABLE project ADD COLUMN audit_by_id BIGINT COMMENT '立项审核人user_id';
