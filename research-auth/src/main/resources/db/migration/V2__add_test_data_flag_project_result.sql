-- project_result 表新增 test_data_flag（列已存在则跳过）
SET NAMES utf8mb4;

SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'project_result' AND COLUMN_NAME = 'test_data_flag');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE project_result ADD COLUMN test_data_flag TINYINT DEFAULT 0 COMMENT ''是否测试数据（0-否/1-是）''', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @idx_exists = (SELECT COUNT(*) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'project_result' AND INDEX_NAME = 'idx_test_data_flag');
SET @sql = IF(@idx_exists = 0, 'CREATE INDEX idx_test_data_flag ON project_result(test_data_flag)', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
