# 科研项目协作平台 - 后端启动脚本（PowerShell）
# 使用前请确保：MySQL(3306)、Redis(6379) 已启动；可选 Nacos(8848)
# 在 research-parent 目录执行：先 mvn install -DskipTests 再运行本脚本

$baseDir = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
$parentDir = Join-Path $baseDir "research-parent"

Write-Host "后端根目录: $parentDir" -ForegroundColor Cyan
Write-Host "按顺序启动：认证(8091) -> 网关(8090) -> 项目(8083) -> 任务(8084)" -ForegroundColor Yellow
Write-Host ""

# 1. 认证服务
Write-Host "[1/4] 启动 research-auth :8091 ..." -ForegroundColor Green
$authDir = Join-Path $baseDir "research-auth"
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$authDir'; mvn spring-boot:run -DskipTests"
Start-Sleep -Seconds 20

# 2. 网关
Write-Host "[2/4] 启动 research-gateway :8090 ..." -ForegroundColor Green
$gatewayDir = Join-Path $baseDir "research-gateway"
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$gatewayDir'; mvn spring-boot:run -DskipTests"
Start-Sleep -Seconds 15

# 3. 项目服务
Write-Host "[3/4] 启动 research-project :8083 ..." -ForegroundColor Green
$projectDir = Join-Path $baseDir "research-project"
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$projectDir'; mvn spring-boot:run -DskipTests"
Start-Sleep -Seconds 12

# 4. 任务服务
Write-Host "[4/4] 启动 research-task :8084 ..." -ForegroundColor Green
$taskDir = Join-Path $baseDir "research-task"
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$taskDir'; mvn spring-boot:run -DskipTests"

Write-Host ""
Write-Host "后端服务已在独立窗口中启动。网关地址: http://localhost:8090" -ForegroundColor Cyan
Write-Host "可选：research-achievement(:8085)、research-document(:8086) 可单独 mvn spring-boot:run 启动。" -ForegroundColor Gray
