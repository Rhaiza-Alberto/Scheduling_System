# Quick test script - Run this in PowerShell

# Find adb path
$adbPath = "C:\Users\alely\AppData\Local\Android\Sdk\platform-tools\adb.exe"

# Check if adb exists
if (-not (Test-Path $adbPath)) {
    Write-Host "ERROR: adb not found at $adbPath" -ForegroundColor Red
    Write-Host ""
    Write-Host "Try one of these:" -ForegroundColor Yellow
    Write-Host "1. Use Android Studio Terminal instead (View → Tool Windows → Terminal)" -ForegroundColor Yellow
    Write-Host "2. Find your Android SDK path and update this script" -ForegroundColor Yellow
    Write-Host "3. Add Android SDK to PATH environment variable" -ForegroundColor Yellow
    exit 1
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Android Emulator Connection Test" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Test 1: Check adb version
Write-Host "[1/4] Checking adb..." -ForegroundColor Yellow
& $adbPath --version
Write-Host ""

# Test 2: List devices
Write-Host "[2/4] Checking connected devices..." -ForegroundColor Yellow
& $adbPath devices
Write-Host ""

# Test 3: Ping from emulator
Write-Host "[3/4] Pinging 10.0.2.2 from emulator..." -ForegroundColor Yellow
& $adbPath shell ping -c 4 10.0.2.2
if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ SUCCESS: Emulator can reach host!" -ForegroundColor Green
} else {
    Write-Host "✗ FAILED: Emulator cannot reach host" -ForegroundColor Red
    Write-Host ""
    Write-Host "Try these fixes:" -ForegroundColor Yellow
    Write-Host "1. Restart emulator: & '$adbPath' emu kill" -ForegroundColor Yellow
    Write-Host "2. Check emulator type (must be Android Emulator, not Google Play)" -ForegroundColor Yellow
    Write-Host "3. Check Windows Firewall allows port 80" -ForegroundColor Yellow
}
Write-Host ""

# Test 4: Test HTTP
Write-Host "[4/4] Testing HTTP connection..." -ForegroundColor Yellow
& $adbPath shell curl -s http://10.0.2.2/scheduling-api/test.php
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Test Complete" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "If all tests passed, run your Android app!" -ForegroundColor Green
