# 多角色首页与项目/文档识别实现说明

## 一、已修改/新增的代码

### 1. 数据库（SQL）

- **`sql/schema.sql`**
  - **project_member**：增加 `role`（LEADER/MEMBER/VISITOR）、`status`（1=已加入 0=待审批）、`join_time`。
  - 新增 **project_apply**：项目申请加入（PENDING/APPROVED/REJECTED），供「主动申请-负责人审批」使用。
  - 新增 **document_meta**：文档元数据表，含 `project_id`、`creator_id`、`permission_type`，用于「身份绑定-项目关联-文档归属」识别。
- **`sql/migrate_project_member_and_doc.sql`**（新建）：在**已有库**上执行一次，为旧表补列并创建新表（不删数据）。

### 2. 后端（research-project）

- **`entity/ProjectMember.java`**（新建）：项目成员实体，含 `projectId`、`userId`、`role`、`status`、`joinTime`。
- **`mapper/ProjectMemberMapper.java`**（新建）：`selectProjectIdsByUserId` 等。
- **`mapper/ProjectMemberMapper.xml`**（新建）：对应 SQL。
- **`service/impl/ProjectServiceImpl.java`**：
  - 创建项目时自动插入 **project_member**（负责人为 LEADER、status=1），实现「自主创建即绑定」。
  - 注入 `ProjectMemberMapper`，在 `createProject` 中 `projectMemberMapper.insert(pm)`。
- **`controller/ProjectController.java`**：
  - 新增 **GET /project/my**：根据当前用户 ID 查询「我参与的项目」（依赖网关/下游传入 userId，见下文）。
- **`mapper/ProjectMapper.xml`**：
  - 分页查询中 `memberId` 条件使用 `COALESCE(pm.status, 1) = 1`，兼容有/无 `status` 列。

### 3. 后端（research-document）

- **`entity/Document.java`**：增加 `projectId`、`creatorId`、`permissionType` 及 `name`、`filePath`、`fileType`、`version`、`delFlag`，与 document_meta 表及开题报告「文档归属」设计一致。

### 4. 前端

- **`src/views/dashboard/DashboardHome.vue`**（新建）：
  - **负责人（LEADER）**：项目总览、任务进度、文档与成果统计 + 右侧「创建项目」、待办、最近动态。
  - **科研人员（MEMBER）**：参与项目概览（表格）、个人任务统计 + 项目申请入口、个人待办、我的成果。
  - **访客（VISITOR）**：授权项目列表、授权文档说明 + 右侧「访客权限说明」。
  - 数据来源：`getMyProjects()`、`getProjectStatistics()`、`getTaskList()` 等。
- **`src/router/index.js`**：
  - 默认进入由 `/dashboard/profile` 改为 **`/dashboard/home`**，新增子路由 `home` → `DashboardHome`。
- **`src/views/Home.vue`**：
  - 首页菜单项指向 **`/dashboard/home`**。
  - **访客**：侧栏仅显示「项目查看」「文档预览」「成果查看」+ 个人中心、通知（无创建项目/任务/提交成果等）。
  - **负责人/科研人员**：保留项目管理、任务协作、文档共享、成果归档、通知、全局搜索；负责人额外显示「创建项目」「立项审核」「创建任务」等。
- **`src/api/project.js`**：新增 **`getMyProjects(params)`**，请求 **GET /api/project/my**。

---

## 二、你需要动手做的事

### 1. 数据库

- **新环境或可清空库**：执行整份 **`sql/schema.sql`**（会 DROP 再 CREATE，含 project_member 新列、project_apply、document_meta）。
- **已有库且要保留数据**：执行一次 **`sql/migrate_project_member_and_doc.sql`**（为 project_member 补 role/status/join_time，并创建 project_apply、document_meta）。若表已是新结构，可跳过或只执行其中部分语句。

### 2. 后端「我参与的项目」接口（/project/my）用到的 userId

- **GET /project/my** 内部使用 **`SecurityUtils.getUserId()`** 作为当前用户 ID。
- 若网关或下游未设置 `SecurityUtils`（例如未从 JWT 解析并写入 ThreadLocal），则当前为默认值 1，即「我参与的项目」会按用户 1 查。
- **建议**：在网关或项目服务请求入口，从 JWT 解析出 `userId`，写入 **`SecurityUtils.setUserId(userId)`**（或通过请求头如 `X-User-Id` 在项目服务中读取并 set），这样 `/project/my` 和首页「参与项目概览」才会按登录用户正确过滤。

### 3. 前端登录后保存 userId（可选但推荐）

- 登录接口若返回 **userId**，在 **Login.vue** 里写入 **`localStorage.setItem('userId', userId)`**，便于后续扩展（如个人中心、申请加入时带 userId）。
- 项目服务若从网关拿不到 JWT 中的 userId，可临时用「前端把 userId 当 query 传给 /project/my」的方式做联调（后端需校验与 token 一致，生产建议仍由网关/服务端从 JWT 取 userId）。

### 4. 项目申请/邀请/文档「我的文档」后续实现（未在本次改动的部分）

- **项目申请**：前端「项目申请入口」跳转到项目列表或「项目广场」；后端需实现 **project_apply** 的提交与负责人审批接口，审批通过时往 **project_member** 插入一条（role 可为 MEMBER/VISITOR）。
- **邀请加入**：负责人选用户邀请 → 用户确认后插入 **project_member**（同上）。
- **文档「我的文档」**：后端文档列表接口按「当前 userId → project_member 得到 project_id 列表 → document_meta 中 project_id + permission_type 匹配」过滤；前端「我的文档」/「项目文档」两个标签按该接口展示即可。

---

## 三、运行与验证

1. 启动 Nacos、MySQL、Redis（及如需的 MongoDB/ES）。
2. 执行上述 SQL（schema 或 migrate）。
3. 启动 **research-auth**、**research-gateway**、**research-project**。
4. 启动前端：`npm run serve`，访问登录页，用 **leader** / **member** / **visitor**（密码 123456）分别登录。
5. 默认进入 **首页（/dashboard/home）**：
   - **leader**：看到项目总览、任务进度、文档与成果统计 + 右侧「创建项目」、待办、最近动态。
   - **member**：看到参与项目概览、个人任务统计 + 项目申请入口、个人待办、我的成果。
   - **visitor**：看到授权项目列表、授权文档说明 + 访客权限说明。
6. 侧栏：访客仅「项目查看」「文档预览」「成果查看」+ 个人中心、通知；负责人/科研人员为完整菜单（负责人多「创建项目」等）。
7. 创建项目：用负责人账号在首页或项目列表点击「创建项目」，创建成功后，该负责人会自动出现在 **project_member**（role=LEADER），从而在「我参与的项目」中可见。

按上述步骤操作后，多角色首页与核心模块展示逻辑即可与开题报告一致；新用户注册后通过「申请/邀请/创建」绑定项目，再配合文档元数据（project_id、creator_id、permission_type）即可实现「识别自己项目文档」的机制。
