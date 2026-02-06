-- 项目详情同步表（存储文档/成果列表快照，由 RabbitMQ 同步更新）
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS project_detail (
    id BIGINT NOT NULL AUTO_INCREMENT,
    project_id BIGINT NOT NULL COMMENT '项目ID',
    document_ids VARCHAR(2000) COMMENT '文档ID列表，逗号分隔（仅 test_data_flag=0）',
    result_ids VARCHAR(2000) COMMENT '成果ID列表，逗号分隔（仅 test_data_flag=0）',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_project_id (project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '项目详情同步表';
