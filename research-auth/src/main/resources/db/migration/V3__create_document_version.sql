-- 文档版本表（MySQL 存版本元数据，文件内容在 MongoDB）
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS document_version (
    id BIGINT NOT NULL AUTO_INCREMENT,
    document_id BIGINT NOT NULL COMMENT 'document_meta.id',
    version_no VARCHAR(20) NOT NULL COMMENT '版本号如 1.0、2.0',
    mongo_file_id VARCHAR(64) COMMENT 'MongoDB GridFS 文件ID',
    upload_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    change_desc VARCHAR(500) COMMENT '变更说明',
    uploader_id BIGINT COMMENT '上传人user_id',
    test_data_flag TINYINT DEFAULT 0 COMMENT '是否测试数据（0-否/1-是）',
    PRIMARY KEY (id),
    KEY idx_document_id (document_id),
    KEY idx_test_data_flag (test_data_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '文档版本表';
