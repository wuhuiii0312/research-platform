-- 成果表迁移：统一表结构为 snake_case（project_id 等），解决 Unknown column 'project_id' 报错
-- 执行方式：mysql -u root -p research_platform < migrate_project_result.sql
-- 注意：会先删除已存在的 project_result 表再重建，若有重要数据请先备份

SET NAMES utf8mb4;

DROP TABLE IF EXISTS project_result;

CREATE TABLE project_result (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '成果ID',
  project_id  BIGINT NOT NULL COMMENT '所属项目ID（关联project表）',
  name        VARCHAR(200) NOT NULL COMMENT '成果名称',
  type        VARCHAR(20)  NOT NULL COMMENT '成果类型：PAPER-论文/PATENT-专利/SOFT-软件著作权/REPORT-报告',
  description TEXT COMMENT '成果描述',
  file_url    VARCHAR(500) COMMENT '附件地址（本地路径或OSS链接）',
  status      VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态：PENDING-待审核/PASSED-已通过/ARCHIVED-已归档',
  submitter_id BIGINT NOT NULL COMMENT '提交人ID（关联sys_user表）',
  submit_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  audit_user_id BIGINT COMMENT '审核人ID（项目负责人）',
  audit_time DATETIME COMMENT '审核时间',
  audit_remark VARCHAR(500) COMMENT '审核备注',
  del_flag    INT DEFAULT 0 COMMENT '逻辑删除：0-正常/1-删除',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  KEY idx_project (project_id),
  KEY idx_submitter (submitter_id),
  KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '科研项目成果表';
