-- 1. sys_user 增加五位数字邀请码（新用户注册时生成；已有用户可为空，后续可在个人中心补生成）
USE research_platform;

-- 若列已存在会报错，可忽略
ALTER TABLE sys_user ADD COLUMN invite_code VARCHAR(5) NULL COMMENT '五位数字邀请码，供负责人邀请入项目';

-- 2. project 增加是否对外公开
ALTER TABLE project ADD COLUMN is_public TINYINT DEFAULT 0 COMMENT '0-不公开仅成员可见 1-对外公开所有人可浏览';
