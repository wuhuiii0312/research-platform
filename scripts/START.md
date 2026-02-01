# 科研项目协作平台 - 启动说明

## 一、环境准备

1. **MySQL 8**：端口 3306，创建数据库并执行建表脚本  
   ```bash
   mysql -u root -p < sql/schema.sql
   ```
2. **Redis**：端口 6379（认证服务登录、验证码等需要）
3. **Nacos**（可选）：端口 8848。若不启动 Nacos，网关使用静态路由，认证/项目/任务服务会尝试连接 Nacos，连接失败可能影响启动，可暂时忽略或关闭各服务中的 Nacos 配置。

## 二、后端启动

### 方式一：使用脚本（推荐）

在项目根目录（cbs）下执行：

```powershell
.\scripts\start-backend.ps1
```

会依次在新窗口中启动：**认证(8091) → 网关(8090) → 项目(8083) → 任务(8084)**。

### 方式二：手动逐个启动

需先**从项目根目录**执行一次 `mvn install -DskipTests`（构建 research-common、research-gateway），再**进入 research-parent** 执行 `mvn install -DskipTests`（构建 research-auth、research-project、research-task 等）。然后方可各模块目录下执行 `mvn spring-boot:run -DskipTests`。**若仍出现 Port 8081/8082 already in use**，请先执行 `mvn clean` 再 `mvn spring-boot:run -DskipTests`，以刷新已复制的配置。

```bash
# 终端1 - 认证服务（在 research-auth 目录）
cd research-auth
mvn spring-boot:run -DskipTests

# 终端2 - 网关（在 research-gateway 目录，等认证启动后再开）
cd research-gateway
mvn spring-boot:run -DskipTests

# 终端3 - 项目服务（在 research-project 目录）
cd research-project
mvn spring-boot:run -DskipTests

# 终端4 - 任务服务（在 research-task 目录）
cd research-task
mvn spring-boot:run -DskipTests
```

**端口一览**

| 服务           | 端口  |
|----------------|-------|
| research-auth  | 8091  |
| research-gateway| 8090 |
| research-project| 8083 |
| research-task  | 8084  |
| research-achievement | 8085 |
| research-document   | 8086 |

前端通过 **网关 8090** 访问后端，请求前缀 `/api` 会转发到对应微服务。

## 三、前端启动

```bash
cd research-frontend
npm install
npm run serve
```

浏览器访问：**http://localhost:8080**（Vue CLI 默认端口）  
默认账号：**admin / admin123** 或 **zhangsan / admin123**（以 sql/schema.sql 中密码为准）。

## 四、验证

- 前端：http://localhost:8080 → 登录页 → 登录后进入首页
- 网关：http://localhost:8090
- 直接调登录接口（Postman 等）：`POST http://localhost:8090/api/user/login`，Body: `{"username":"admin","password":"admin123"}`

## 五、端口被占用 / 内存不足

### 端口 8091 或 8090 已被占用

当前默认为认证 **8091**、网关 **8090**。若被占用，可改用 profile `port`（认证 8081、网关 8082）：

```powershell
cd research-auth
mvn spring-boot:run -DskipTests -Dspring.profiles.active=port

cd research-gateway
mvn spring-boot:run -DskipTests -Dspring.profiles.active=port
```

此时前端代理改为 **http://localhost:8082**；网关的 port 配置会转发 /api/user、/api/auth 到 8081。

### 内存不足（“页面文件太小” / “Failed to commit metaspace” / “Could not reserve heap”）

- **Spring Boot 进程**：各模块已在 `pom.xml` 中配置极低内存（`-Xmx128m -Xms24m -XX:MaxMetaspaceSize=48m -XX:+UseSerialGC`），直接运行即可。
- **Maven 进程**：各模块目录下已添加 `.mvn/jvm.config`，执行 `mvn spring-boot:run` 时 Maven 自身也会使用较小内存，避免“Scanning for projects”阶段 OOM。

```powershell
cd research-auth
mvn spring-boot:run -DskipTests
```

**若内存非常紧张**：建议一次只启动一个服务（先关掉其他已启动的 Java 进程），再按顺序启动：认证 → 网关 → 项目 → 任务。

父工程下已有 `.mvn/jvm.config`，执行 `mvn install -DskipTests` 时 Maven 使用较小内存。若仍报错，可先设置环境变量：

```powershell
$env:MAVEN_OPTS="-Xmx512m -Xms256m -XX:+UseSerialGC"
cd research-parent
mvn install -DskipTests
```
