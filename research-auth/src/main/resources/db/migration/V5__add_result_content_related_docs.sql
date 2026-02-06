-- 成果表增加 result_content、related_document_ids（列已存在则跳过）
SET NAMES utf8mb4;

SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'project_result' AND COLUMN_NAME = 'result_content');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE project_result ADD COLUMN result_content TEXT COMMENT ''成果正文内容'' AFTER description', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'project_result' AND COLUMN_NAME = 'related_document_ids');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE project_result ADD COLUMN related_document_ids VARCHAR(1000) COMMENT ''关联文档ID列表，逗号分隔''', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
