-- 项目表增加 5 位随机项目 ID（10000-99999），唯一；已有数据可暂为 NULL
USE research_platform;
ALTER TABLE project ADD COLUMN project_no INT NULL COMMENT '5位项目编号，10000-99999，唯一' AFTER id;
ALTER TABLE project ADD UNIQUE KEY uk_project_no (project_no);
