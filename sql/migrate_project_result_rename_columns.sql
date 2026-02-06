-- 仅当 project_result 表列为 camelCase（报 Unknown column 'project_id'）时执行本脚本，将列名改为 snake_case
-- 执行前请备份。执行方式：mysql -u root -p research_platform < migrate_project_result_rename_columns.sql
-- 若执行时报 Unknown column 'projectId'，说明表已是 snake_case，请改执行 migrate_project_result.sql 重建表即可

SET NAMES utf8mb4;

ALTER TABLE project_result CHANGE COLUMN projectId project_id BIGINT NOT NULL COMMENT '所属项目ID';
ALTER TABLE project_result CHANGE COLUMN fileUrl file_url VARCHAR(500) COMMENT '附件地址';
ALTER TABLE project_result CHANGE COLUMN submitterId submitter_id BIGINT NOT NULL COMMENT '提交人ID';
ALTER TABLE project_result CHANGE COLUMN submitTime submit_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间';
ALTER TABLE project_result CHANGE COLUMN auditUserId audit_user_id BIGINT COMMENT '审核人ID';
ALTER TABLE project_result CHANGE COLUMN auditTime audit_time DATETIME COMMENT '审核时间';
ALTER TABLE project_result CHANGE COLUMN auditRemark audit_remark VARCHAR(500) COMMENT '审核备注';
ALTER TABLE project_result CHANGE COLUMN delFlag del_flag INT DEFAULT 0 COMMENT '0-正常/1-删除';
ALTER TABLE project_result CHANGE COLUMN createTime create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';
ALTER TABLE project_result CHANGE COLUMN updateTime update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';
