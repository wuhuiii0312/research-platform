# 中间件初始化清单

## 1. Nacos

- 服务注册：research-auth、research-user、research-project、research-document、research-task、research-search、research-notification、research-gateway
- 命名空间：research-platform（与各服务 application.yml 一致）

## 2. 项目详情同步（已移除 RabbitMQ，改为 HTTP 调用）

- **文档服务**：上传文档后通过 `RestTemplate` 调用项目服务的 `/project/detail/sync/document` 接口
- **项目服务**：提交成果后直接调用 `ProjectDetailService.syncResult`（同服务内）
- **同步目标**：更新 `project_detail` 表的 `document_ids` 和 `result_ids` 字段
- **服务发现**：文档服务通过 Nacos 服务发现调用项目服务（需确保 Nacos 正常运行）

## 3. Seata（可选）

- 事务组：`research_platform_tx_group`
- 在对应环境的 `application.yml` 或 Nacos 配置中配置 `seata.tx-service-group=research_platform_tx_group`

## 4. Elasticsearch

- 索引（需预先创建或由应用首次写入时创建）：
  - `public_document`：字段含 `testDataFlag`(boolean)、`is_public`(boolean)、`title`、`content`、`create_time` 等
  - `public_result`：字段含 `testDataFlag`(boolean)、`is_public`(boolean)、`name`、`description`、`create_time` 等
- 公开检索时过滤条件：`is_public=1` 且 `testDataFlag=false`

## 5. Redis

- 用途：JWT 黑名单、限流、缓存
- 建议：缓存非测试数据时设置过期时间 1 小时，key 可带业务前缀避免与测试数据混合

## 6. Flyway（research-auth）

- 脚本目录：`classpath:db/migration`
- 执行顺序：V1～V7（V7 为测试数据清理）
- 配置：`spring.flyway.enabled=true`，`baseline-on-migrate=true`
