-- 清理测试数据（test_data_flag=1 + 名称含“测试”或无归属项目）
-- 文档主表：按 test_data_flag 清理
DELETE FROM document_meta WHERE test_data_flag = 1;
-- 成果表：按 test_data_flag 清理
DELETE FROM project_result WHERE test_data_flag = 1;
-- 批注表：按 test_data_flag 清理
DELETE FROM document_annotation WHERE test_data_flag = 1;

-- 额外清理：名称包含“测试”或无归属项目的任务/文档/成果
DELETE FROM task WHERE name LIKE '测试%' OR project_id IS NULL;
DELETE FROM document_meta WHERE project_id IS NULL OR name LIKE '测试%';
DELETE FROM project_result WHERE project_id IS NULL OR name LIKE '测试%';