# 移除 RabbitMQ 说明

## 修改内容

已将所有 RabbitMQ 相关代码移除，改为项目内同步方案：

### 1. 项目服务（research-project）

- **新增**：`ProjectDetailService` 和 `ProjectDetailServiceImpl`，提供 `syncDocument` 和 `syncResult` 方法
- **新增**：`ProjectDetailController`，暴露同步接口 `/project/detail/sync/document` 和 `/project/detail/sync/result`
- **修改**：`ProjectResultServiceImpl` 提交成果后直接调用 `projectDetailService.syncResult`（同服务内调用）
- **删除**：`ProjectSyncListener`（RabbitMQ 监听器）、`ProjectRabbitConfig`（RabbitMQ 配置）

### 2. 文档服务（research-document）

- **修改**：`DocumentServiceImpl` 上传文档后通过 `RestTemplate` 调用项目服务的同步接口
- **新增**：`RestTemplateConfig`，配置 `@LoadBalanced RestTemplate`（支持 Nacos 服务发现）
- **删除**：`RabbitMQConfig`（RabbitMQ 配置）
- **移除**：`application.yml` 中的 `rabbitmq` 配置

### 3. 依赖变更

- **research-document/pom.xml**：移除 `spring-boot-starter-amqp`，新增 `spring-cloud-starter-loadbalancer`
- **research-project/pom.xml**：移除 `spring-boot-starter-amqp`
- **research-parent/pom.xml**：移除 RabbitMQ 依赖管理，新增 LoadBalancer 依赖管理

### 4. 删除文件

- `research-common/src/main/java/com/research/common/core/domain/SyncMessage.java`（不再需要）

## 同步流程

### 文档上传同步

1. 文档服务上传文档成功后
2. 调用 `syncDocumentToProjectDetail(projectId, documentId)`
3. 通过 `RestTemplate` 调用 `http://research-project/project/detail/sync/document`（使用 Nacos 服务发现）
4. 项目服务更新 `project_detail` 表的 `document_ids` 字段

### 成果提交同步

1. 项目服务提交成果成功后
2. 直接调用 `projectDetailService.syncResult(projectId, resultId)`（同服务内）
3. 更新 `project_detail` 表的 `result_ids` 字段

## 优势

- **简化架构**：无需部署和维护 RabbitMQ
- **同步调用**：立即同步，无需等待消息队列
- **服务发现**：通过 Nacos 自动发现项目服务地址
- **容错处理**：同步失败仅记录日志，不影响主流程

## 注意事项

- 确保 Nacos 服务发现正常工作，文档服务才能调用项目服务
- 如果项目服务和文档服务在同一进程，可考虑直接注入 `ProjectDetailService`（当前未实现）
- 同步失败不影响文档/成果的创建，仅影响 `project_detail` 表的更新
