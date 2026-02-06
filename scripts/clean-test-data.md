# 测试数据清理说明

## 一、MySQL 清理（Flyway + 手动接口）

- **初始化时**：Flyway 脚本 `V7__clean_test_data_mysql.sql` 会在认证服务首次启动时执行，清理 `test_data_flag=1` 的 MySQL 数据。
- **手动触发**：调用 `POST /api/system/clear/testData`（需管理员登录并在请求头带 JWT），会执行：
  - `DELETE FROM document_annotation WHERE test_data_flag=1`
  - `DELETE FROM document_version WHERE test_data_flag=1`
  - `DELETE FROM document_meta WHERE test_data_flag=1`
  - `DELETE FROM project_result WHERE test_data_flag=1`

## 二、MongoDB 清理（需在 MongoDB 或文档服务中执行）

在 MongoDB 客户端或文档服务中执行：

```javascript
// document_files 集合（若使用）
db.document_files.deleteMany({ testDataFlag: true });

// result_attachments 集合（若使用）
db.result_attachments.deleteMany({ testDataFlag: true });
```

当前文档文件存储在 GridFS（fs.files/fs.chunks），未单独建 document_files 集合时，可按 metadata 或业务关联在应用层清理。

## 三、Elasticsearch 清理

在 Kibana 或 curl 中执行：

```json
POST /public_document/_delete_by_query
{ "query": { "term": { "testDataFlag": true } } }

POST /public_result/_delete_by_query
{ "query": { "term": { "testDataFlag": true } } }
```

## 四、接口过滤约定

- 所有文档/成果/批注**查询**接口已强制添加 `test_data_flag=0` 条件，新用户仅能看到非测试数据。
- 公开检索 `GET /api/search/public` 仅返回 `is_public=1` 且 `testDataFlag=false` 的数据。
