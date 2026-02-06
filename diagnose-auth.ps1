# 强制UTF-8编码，避免中文乱码
chcp 65001 > $null

# 极简版：仅验证语法结构，无业务逻辑错误
Write-Host "=== 认证服务诊断 ===" -ForegroundColor Cyan
Write-Host "测试时间: $(Get-Date)" -ForegroundColor Gray
Write-Host ""

# 定义测试端点（简化）
$endpoints = @("/", "/actuator/health")

# foreach循环：语法完整闭合
foreach ($endpoint in $endpoints) {
    $url = "http://localhost:8081$endpoint"
    Write-Host "测试: $url" -ForegroundColor Yellow
    
    # try/catch：语法完整，无缺失
    try {
        $response = Invoke-WebRequest -Uri $url -Method Get -TimeoutSec 5 -ErrorAction Stop
        Write-Host "✅ 测试成功，状态码: $($response.StatusCode)" -ForegroundColor Green
    } catch {
        Write-Host "❌ 测试失败: $($_.Exception.Message)" -ForegroundColor Red
    }
    
    Write-Host ""
}

# 端口检查：语法完整闭合
Write-Host "=== 端口检查 ===" -ForegroundColor Cyan
$portInfo = netstat -ano | findstr :8081
if ($portInfo) {
    Write-Host "端口8081已占用" -ForegroundColor Green
} else {
    Write-Host "端口8081未占用" -ForegroundColor Red
}

Write-Host "=== 诊断完成 ===" -ForegroundColor Cyan