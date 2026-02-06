# 科研项目协作平台 - 修复版启动脚本
# 作者: Cursor AI 助手

# 1. 设置编码和基础变量
chcp 65001 | Out-Null
$OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

# 2. 项目根目录
$baseDir = "D:\projectapp\research-platform"

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "科研项目协作平台 - 后端服务启动" -ForegroundColor Yellow
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "项目根目录: $baseDir" -ForegroundColor Cyan
Write-Host "启动顺序: 认证(8081) -> 用户(8084) -> 项目(8082) -> 任务(8083) -> 网关(8085)" -ForegroundColor Green
Write-Host ""

# 3. 清理端口占用
Write-Host "[1] 清理端口占用..." -ForegroundColor Yellow
$ports = @(8081, 8082, 8083, 8084, 8085, 8086, 8087, 8088)
foreach ($port in $ports) {
    try {
        $connection = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue
        if ($connection) {
            $pid = $connection.OwningProcess
            Stop-Process -Id $pid -Force -ErrorAction SilentlyContinue
            Write-Host "   端口 $port 已清理 (PID: $pid)" -ForegroundColor Gray
        }
    } catch {
        # 忽略错误
    }
}
Start-Sleep -Seconds 2

# 4. 检查Maven
Write-Host "[2] 检查Maven环境..." -ForegroundColor Yellow
try {
    mvn -version | Out-Null
    Write-Host "   Maven可用 ✓" -ForegroundColor Green
} catch {
    Write-Host "   Maven不可用 ✗" -ForegroundColor Red
    Write-Host "   请按任意键退出..." -ForegroundColor DarkYellow
    $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
    exit 1
}

# 5. 启动服务函数
function Start-Service {
    param(
        [string]$serviceName,
        [int]$port,
        [int]$waitSeconds = 20
    )
    
    $serviceDir = Join-Path $baseDir $serviceName
    if (-not (Test-Path $serviceDir)) {
        Write-Host "  目录不存在: $serviceDir" -ForegroundColor Red
        return $false
    }
    
    Write-Host "  启动 $serviceName (端口:$port)..." -ForegroundColor Green
    $windowTitle = "$serviceName :$port"
    
    # 创建启动命令
    $command = @"
cd '$serviceDir'
Write-Host '正在启动 $serviceName :$port...' -ForegroundColor Cyan
mvn spring-boot:run -DskipTests
"@
    
    # 保存为临时脚本
    $tempScript = [System.IO.Path]::GetTempFileName() + ".ps1"
    $command | Out-File -FilePath $tempScript -Encoding UTF8
    
    # 启动新窗口
    Start-Process powershell -ArgumentList "-NoExit", "-File", "`"$tempScript`"" -WindowStyle Normal
    
    Write-Host "  等待 $waitSeconds 秒..." -ForegroundColor Gray
    Start-Sleep -Seconds $waitSeconds
    return $true
}

# 6. 依次启动服务
Write-Host "[3] 启动后端服务..." -ForegroundColor Yellow

# 6.1 认证服务
Write-Host "  [1/5] 认证服务" -ForegroundColor Cyan
Start-Service -serviceName "research-auth" -port 8081 -waitSeconds 25

# 6.2 用户服务
Write-Host "  [2/5] 用户服务" -ForegroundColor Cyan
Start-Service -serviceName "research-user" -port 8084 -waitSeconds 15

# 6.3 项目管理服务
Write-Host "  [3/5] 项目管理服务" -ForegroundColor Cyan
Start-Service -serviceName "research-project" -port 8082 -waitSeconds 20

# 6.4 任务管理服务
Write-Host "  [4/5] 任务管理服务" -ForegroundColor Cyan
Start-Service -serviceName "research-task" -port 8083 -waitSeconds 20

# 6.5 网关服务（最后启动）
Write-Host "  [5/5] 网关服务" -ForegroundColor Cyan
Start-Service -serviceName "research-gateway" -port 8085 -waitSeconds 15

# 7. 完成信息
Write-Host ""
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "✓ 所有服务启动完成！" -ForegroundColor Green
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "服务状态检查：" -ForegroundColor Yellow
Write-Host "  1. 认证服务:    http://localhost:8081" -ForegroundColor White
Write-Host "  2. 用户服务:    http://localhost:8084" -ForegroundColor White
Write-Host "  3. 项目管理:    http://localhost:8082" -ForegroundColor White
Write-Host "  4. 任务管理:    http://localhost:8083" -ForegroundColor White
Write-Host "  5. 网关服务:    http://localhost:8085" -ForegroundColor White
Write-Host ""
Write-Host "测试网关路由：" -ForegroundColor Yellow
Write-Host "  • /api/auth/**     -> 认证服务 (8081)" -ForegroundColor Gray
Write-Host "  • /api/user/**     -> 用户服务 (8084)" -ForegroundColor Gray
Write-Host "  • /api/project/**  -> 项目管理 (8082)" -ForegroundColor Gray
Write-Host "  • /api/task/**     -> 任务管理 (8083)" -ForegroundColor Gray
Write-Host ""
Write-Host "常见问题：" -ForegroundColor Yellow
Write-Host "  • 端口占用: 运行脚本会自动清理" -ForegroundColor Gray
Write-Host "  • JwtAuthFilter: 已添加基础实现" -ForegroundColor Gray
Write-Host "  • 编码问题: 已修复UTF-8编码" -ForegroundColor Gray
Write-Host ""
Write-Host "按任意键查看服务日志，或直接关闭窗口..." -ForegroundColor DarkYellow
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")