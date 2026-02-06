-- document_meta 表新增 test_data_flag、is_public（可重复执行：列已存在则跳过）
SET NAMES utf8mb4;

-- test_data_flag
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'document_meta' AND COLUMN_NAME = 'test_data_flag');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE document_meta ADD COLUMN test_data_flag TINYINT DEFAULT 0 COMMENT ''是否测试数据（0-否/1-是）''', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- is_public
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'document_meta' AND COLUMN_NAME = 'is_public');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE document_meta ADD COLUMN is_public TINYINT DEFAULT 0 COMMENT ''0-私有 1-公开''', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 索引（忽略已存在）
SET @idx_exists = (SELECT COUNT(*) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'document_meta' AND INDEX_NAME = 'idx_doc_test_data_flag');
SET @sql = IF(@idx_exists = 0, 'CREATE INDEX idx_doc_test_data_flag ON document_meta(test_data_flag)', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
