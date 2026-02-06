-- 文档批注表
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS document_annotation (
    id BIGINT NOT NULL AUTO_INCREMENT,
    document_id BIGINT NOT NULL COMMENT 'document_meta.id',
    version_no VARCHAR(20) COMMENT '对应版本号',
    content TEXT COMMENT '批注内容',
    position_json VARCHAR(2000) COMMENT '位置信息JSON',
    creator_id BIGINT NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    del_flag INT DEFAULT 0 COMMENT '0-正常 1-已删除',
    test_data_flag TINYINT DEFAULT 0 COMMENT '是否测试数据（0-否/1-是）',
    PRIMARY KEY (id),
    KEY idx_document_id (document_id),
    KEY idx_test_data_flag (test_data_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '文档批注表';
