# =========================================================
# Script: Chạy Spring Boot với Environment Variables
# =========================================================
# Cách sử dụng: 
#   .\run-with-env.ps1
# =========================================================

Write-Host "=================================================" -ForegroundColor Cyan
Write-Host " LOADING ENVIRONMENT VARIABLES FROM .env" -ForegroundColor Cyan
Write-Host "=================================================" -ForegroundColor Cyan

# Kiểm tra file .env tồn tại
if (-Not (Test-Path ".env")) {
    Write-Host "❌ ERROR: File .env không tồn tại!" -ForegroundColor Red
    Write-Host "   Vui lòng tạo file .env từ .env.example" -ForegroundColor Yellow
    exit 1
}

# Đọc file .env và set biến môi trường
Get-Content .env | ForEach-Object {
    $line = $_.Trim()
    
    # Bỏ qua dòng trống và comment
    if ($line -and !$line.StartsWith("#")) {
        # Parse key=value
        $parts = $line -split "=", 2
        if ($parts.Length -eq 2) {
            $key = $parts[0].Trim()
            $value = $parts[1].Trim()
            
            # Set environment variable
            [Environment]::SetEnvironmentVariable($key, $value, "Process")
            Write-Host "✓ Set $key" -ForegroundColor Green
        }
    }
}

Write-Host "`n=================================================" -ForegroundColor Cyan
Write-Host " STARTING SPRING BOOT APPLICATION" -ForegroundColor Cyan
Write-Host "=================================================" -ForegroundColor Cyan

# Chạy Maven
mvn spring-boot:run

