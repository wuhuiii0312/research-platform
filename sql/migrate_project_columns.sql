-- 为 project 表补充 description、start_time、end_time（解决 Unknown column 'description' 等报错）
-- 已有数据库执行一次即可；若某列已存在会报 Duplicate column，可跳过该条或先检查表结构
USE research_platform;

-- 若报 Duplicate column name，说明该列已存在，可忽略
ALTER TABLE project ADD COLUMN description TEXT COMMENT '项目描述' AFTER name;
ALTER TABLE project ADD COLUMN start_time DATE COMMENT '开始时间' AFTER status;
ALTER TABLE project ADD COLUMN end_time DATE COMMENT '结束时间' AFTER start_time;
