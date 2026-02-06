# 简化版启动脚本
$baseDir = Split-Path -Parent $PSScriptRoot

Write-Host "启动后端服务..." -ForegroundColor Yellow

# 启动认证服务
Write-Host "[1] 启动认证服务(8081)" -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$baseDir\research-auth'; mvn spring-boot:run -DskipTests" -WindowStyle Normal
Start-Sleep -Seconds 25

# 启动网关服务
Write-Host "[2] 启动网关服务(9527)" -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$baseDir\research-gateway'; mvn spring-boot:run -DskipTests" -WindowStyle Normal
Start-Sleep -Seconds 18

# 启动项目管理服务
Write-Host "[3] 启动项目管理服务(8082)" -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$baseDir\research-project'; mvn spring-boot:run -DskipTests" -WindowStyle Normal
Start-Sleep -Seconds 15

# 启动任务管理服务
Write-Host "[4] 启动任务管理服务(8083)" -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$baseDir\research-task'; mvn spring-boot:run -DskipTests" -WindowStyle Normal

Write-Host "核心服务已启动完成！" -ForegroundColor Cyan
Write-Host "Gateway: http://localhost:9527 (test: curl http://localhost:9527/auth/test)" -ForegroundColor White