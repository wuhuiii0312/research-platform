# 科研项目协作平台 - 启动说明

## 一、环境准备（必须手动完成）

### 1. MySQL 8（端口 3306）

- 安装并启动 MySQL，确保 root 可连接。
- **创建数据库并建表**（在项目根目录执行，或使用 Navicat/DBeaver 执行 `sql/schema.sql`）：

```powershell
# 在项目根目录 d:\projectapp\research-platform
mysql -u root -p < sql/schema.sql
```

- 若已存在数据库，可只执行建表部分；脚本会 `DROP TABLE IF EXISTS` 再创建，注意备份数据。
- 默认配置：数据库名 `research_platform`，用户名 `root`，密码 `123456`（在各自 `application.yml` 中可改）。

**测试账号（执行 `sql/schema.sql` 后可用，密码均为 `123456`）**

| 角色       | 用户名   | 密码   | 说明         |
|------------|----------|--------|--------------|
| 项目负责人 | leader   | 123456 | 可创建/编辑项目、任务等 |
| 科研人员   | member   | 123456 | 可参与项目、更新任务进度 |
| 访客       | visitor  | 123456 | 仅查看公开内容       |
| 管理员     | admin    | 123456 | 管理后台             |

登录页请选择对应「角色」后使用上表账号密码登录。

### 2. Redis（端口 6379）

- 安装并启动 Redis。认证服务（登录、验证码、登出黑名单）依赖 Redis。
- Windows 可用 WSL 或 Redis for Windows 便携版；无密码、端口 6379 即可。

### 3. Nacos（网关 lb:// 路由依赖）

- 网关路由使用 `lb://research-auth` 等，**需要从 Nacos 发现服务**。请先启动 Nacos（默认端口 **8848**）。
- 网关与认证服务已配置为 `server-addr: localhost:8848`、`namespace: research-platform`（与其余微服务一致；若 Nacos 使用默认 public，可改为 `namespace: public` 并保证各服务同一命名空间）。
- **命名空间**：各服务已配置 `namespace: research-platform`。若你在 Nacos 控制台创建了名为「research-platform」的命名空间，请把配置里的 `namespace` 填成该命名空间的 **ID**（控制台命名空间列表中的「命名空间 ID」）。若未创建自定义命名空间，可改为空字符串 `""` 或 `public`，并保证网关与认证服务使用同一值。
- **验证 Nacos 注册**（PowerShell 或 Git Bash；`namespaceId` 与配置一致，如 public 或 research-platform 的 ID）：
  ```powershell
  curl "http://localhost:8848/nacos/v1/ns/instance/list?serviceName=research-auth&namespaceId=public"
  curl "http://localhost:8848/nacos/v1/ns/instance/list?serviceName=research-gateway&namespaceId=public"
  ```
  应返回包含实例 IP+端口的 JSON（`hosts` 非空）。**若 research-auth 未启动，第一个接口的 hosts 为空，此时访问 http://localhost:9527/auth/health 会 503。**
- **验证网关转发认证服务**：**必须先启动 research-auth，再启动 research-gateway**。然后浏览器访问 `http://localhost:9527/auth/health`，应返回 `{"status":"UP"}`。若返回 503 或连接错误，说明网关未发现 research-auth 实例，请确认：① Nacos 已启动；② research-auth 已启动且与网关在同一 Nacos 命名空间；③ 上述 curl 能查到 research-auth 实例。

---

## 二、后端启动

### 首次运行：先编译父工程

在项目根目录执行（只需执行一次或依赖变更后执行）：

```powershell
cd d:\projectapp\research-platform\research-parent
mvn clean install -DskipTests
```

### 方式一：使用脚本（推荐）

在项目根目录执行：

```powershell
cd d:\projectapp\research-platform
.\scripts\start-backend.ps1
```

会依次在新窗口启动：**认证(8081) → 网关(9527) → 项目(8082) → 任务(8083)**。每个窗口不要关闭。

### 方式二：手动逐个启动（每个服务一个终端，PowerShell 直接执行）

```powershell
# 终端 1 - 认证服务（端口 8081）
cd d:\projectapp\research-platform\research-auth
mvn spring-boot:run -DskipTests

# 终端 2 - 网关（端口 9527）
cd d:\projectapp\research-platform\research-gateway
mvn spring-boot:run -DskipTests

# 终端 3 - 项目服务（端口 8082）
cd d:\projectapp\research-platform\research-project
mvn spring-boot:run -DskipTests

# 终端 4 - 任务服务（端口 8083）
cd d:\projectapp\research-platform\research-task
mvn spring-boot:run -DskipTests
```

**端口一览（与中间件 8080/3306/6379 无冲突）**

| 服务                   | 端口  |
|------------------------|-------|
| research-auth          | 8081  |
| research-gateway       | 9527  |
| research-project       | 8082  |
| research-task          | 8083  |
| research-user          | 8086  |
| research-document      | 8084  |
| research-search        | 8087  |
| research-notification  | 8088  |

前端通过 **网关 9527** 访问后端：`vue.config.js` 将 `/api` 代理到 `http://localhost:9527`。

---

## 三、前端启动

```powershell
cd d:\projectapp\research-platform\research-frontend
npm install
npm run serve
```

浏览器访问：**http://localhost:8080**（Vue CLI 默认端口，已在 vue.config.js 中配置；若端口冲突会提示改用 8081 等）。

- 前端 `request.js` 使用 `baseURL: '/api'`，开发时通过 `vue.config.js` 将 `/api` 转发到网关 `http://localhost:9527`，因此需**先启动 Nacos、认证、网关**再访问前端页面。
- **验证网关路由**：`curl http://localhost:9527/auth/test` 应返回 `research-auth服务正常响应！`；可选 `curl http://localhost:9527/actuator/gateway/routes` 查看路由列表。

---

## 四、登录与验证

- **默认进入登录页**：前端根路径 `/` 和未登录访问均会跳转到 `/login`；登录成功后跳转至 `/dashboard`（个人中心）。
- **注册功能**：登录页提供「登录/注册」标签切换；注册时账号唯一、密码统一 123456、角色可选 LEADER/MEMBER/VISITOR；注册成功后自动切回登录并填充账号。
- **默认账号**：脚本 `sql/schema.sql` 中插入的用户为 `admin`、`zhangsan`，密码为 BCrypt 加密。若你未改过脚本，常见测试密码为 **123456** 或 **admin123**（与脚本中加密值一致）。可先试 `admin` / `123456` 或 `admin` / `admin123`。
- **通过前端登录**：打开 http://localhost:8080 → 登录页 → 输入用户名密码。前提：Nacos(8848)、认证(8081)、网关(9527) 已启动。
- **不经过网关直接测认证**：`POST http://localhost:8081/login`，Body: `{"username":"admin","password":"123456"}`（认证服务根路径为 `/login`）。

---

## 五、可能遇到的问题与处理

### 1. 端口已被占用

- 报错类似 `Port 9527 was already in use` 或 `BindException`。
- **处理**：关闭占用该端口的进程（如 `netstat -ano | findstr :9527` 查 PID 后 `taskkill /PID xxx /F`），或修改对应服务的 `server.port`，并确保前端代理目标与网关端口一致（当前网关 9527）。

### 2. MySQL 连接失败（Access denied / Communications link failure）

- **处理**：检查 MySQL 已启动；在 `research-auth`、`research-project`、`research-task` 等模块的 `application.yml` 中，将 `spring.datasource.url`、`username`、`password` 改为本机 MySQL 的库名、用户名、密码（库名需为 `research_platform` 或与建表脚本一致）。

### 3. Redis 连接失败（Connection refused）

- 认证服务依赖 Redis（验证码、登出黑名单）。
- **处理**：启动 Redis（端口 6379）；或在开发阶段可暂时注释掉认证里使用 Redis 的代码（不推荐长期）。

### 4. Nacos 注册失败 / 网关访问 503 / auth/health 不返回 {"status":"UP"}

- 网关使用 `lb://research-auth` 等，依赖 Nacos 发现实例。**只注册了 gateway 不够，必须让 research-auth 也注册到 Nacos**，否则访问 `http://localhost:9527/auth/health` 会 503，不会返回 `{"status":"UP"}`。
- **处理**：  
  - 启动 Nacos（端口 8848）；**先启动 research-auth（8081），再启动 research-gateway（9527）**，确保 Nacos 服务列表里同时有 `research-auth` 和 `research-gateway`。  
  - 网关与认证已配置 `namespace: research-platform`（需在 Nacos 控制台创建该命名空间并填写其 **命名空间 ID**，或改用 `""`/`public` 并保证各服务一致）。  
  - 验证：浏览器访问 `http://localhost:9527/auth/health` 应返回 `{"status":"UP"}`；直连认证 `http://localhost:8081/health` 也应返回 `{"status":"UP"}`。  
  - 或直接访问认证服务 `http://localhost:8081/login` 测试（见第四节）。

### 5. 前端请求跨域或 404

- 开发环境已配置 proxy：`/api` → `http://localhost:8085`。请确保 `npm run serve` 使用的是当前项目下的 `vue.config.js`，且网关已启动。
- 若直接改过 baseURL 为网关地址，仍出现跨域，请以「同源 + proxy」方式访问（打开 http://localhost:8080 而不是直接打开 8085）。

### 6. 项目/任务相关接口 404 或 500

- 确保已执行 `sql/schema.sql`（含 `project` 表及 `del_flag`、`create_time` 等字段）。
- 若表结构与实体不一致，请以当前仓库中的 `sql/schema.sql` 为准重建表或增量修改。

### 7. Maven 编译失败（Could not find artifact / 编译错误）

- 在**项目根目录**执行：  
  `cd d:\projectapp\research-platform`  
  再执行：  
  `cd research-parent; mvn clean install -DskipTests`  
  确保 research-common、research-gateway、research-auth 等全部编译通过后，再在各子目录执行 `mvn spring-boot:run -DskipTests`。

### 8. 内存不足（页面文件太小 / G1 virtual space / insufficient memory）

- 报错示例：`os::commit_memory ... failed; error='页面文件太小，无法完成操作。'` 或 `There is insufficient memory for the Java Runtime Environment to continue`。
- **处理**：  
  - 认证、网关、项目、任务四个模块的 `pom.xml` 已配置低内存 JVM 参数（`-Xms128m -Xmx256m -XX:MaxMetaspaceSize=96m -XX:+UseSerialGC`），直接 `mvn spring-boot:run -DskipTests` 即可。  
  - 若仍报错，可适当增大 Windows 虚拟内存（页面文件）：系统属性 → 高级 → 性能设置 → 高级 → 虚拟内存 → 更改，为系统盘或当前盘设置更大的页面文件后重启。  
  - 或一次只启动 1～2 个服务，关闭其他已打开的 Java/IDE 进程以释放内存。

---

## 六、推荐启动顺序（简要）

1. **环境**：启动 MySQL、Redis；若要用网关路由，启动 Nacos。
2. **后端**：`research-parent` 下执行 `mvn install -DskipTests`，再执行 `.\scripts\start-backend.ps1` 或按顺序手动启动 认证 → 网关 → 项目 → 任务。
3. **前端**：`research-frontend` 下执行 `npm install`、`npm run serve`，浏览器打开 http://localhost:8080。
4. **登录**：使用 schema 中的账号（如 admin / 123456）登录；若仅测认证可不经网关直接访问 `POST http://localhost:8081/login`。
