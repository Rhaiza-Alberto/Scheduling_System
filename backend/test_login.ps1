# PowerShell script to test login endpoint
# Run this to verify backend is working before running Android app

Write-Host "=== Scheduling System Login Test ===" -ForegroundColor Cyan
Write-Host ""

# Test 1: Connectivity
Write-Host "[1/3] Testing connectivity..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost/scheduling-api/test.php" -Method Get -TimeoutSec 5
    Write-Host "✓ Backend is reachable (HTTP 200)" -ForegroundColor Green
    Write-Host "Response: $($response.Content)" -ForegroundColor Gray
} catch {
    Write-Host "✗ Backend is unreachable" -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
    Write-Host "Make sure XAMPP is running!" -ForegroundColor Yellow
    exit 1
}

Write-Host ""

# Test 2: Setup verification
Write-Host "[2/3] Verifying setup..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost/scheduling-api/verify_setup.php" -Method Get -TimeoutSec 5
    $content = $response.Content | ConvertFrom-Json
    
    Write-Host "✓ Setup verification complete" -ForegroundColor Green
    
    # Check critical items
    $db = $content.checks.database.connection
    $admin_user = $content.checks.database.test_user_admin
    
    if ($db -eq "OK") {
        Write-Host "  ✓ Database connection: OK" -ForegroundColor Green
    } else {
        Write-Host "  ✗ Database connection: FAILED" -ForegroundColor Red
    }
    
    if ($admin_user -eq "OK") {
        Write-Host "  ✓ Test user (admin): OK" -ForegroundColor Green
    } else {
        Write-Host "  ✗ Test user (admin): MISSING" -ForegroundColor Red
    }
} catch {
    Write-Host "✗ Setup verification failed" -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# Test 3: Login endpoint
Write-Host "[3/3] Testing login endpoint..." -ForegroundColor Yellow
try {
    $body = @{
        username = "admin@example.com"
        password = "password123"
    } | ConvertTo-Json
    
    $response = Invoke-WebRequest -Uri "http://localhost/scheduling-api/login.php" `
        -Method Post `
        -ContentType "application/json" `
        -Body $body `
        -TimeoutSec 5
    
    $content = $response.Content | ConvertFrom-Json
    
    if ($content.success) {
        Write-Host "✓ Login successful!" -ForegroundColor Green
        Write-Host "  User: $($content.user.name)" -ForegroundColor Green
        Write-Host "  Role: $($content.user.account_type)" -ForegroundColor Green
        Write-Host "  ID: $($content.user.person_ID)" -ForegroundColor Green
    } else {
        Write-Host "✗ Login failed: $($content.message)" -ForegroundColor Red
    }
} catch {
    Write-Host "✗ Login test failed" -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "=== Test Complete ===" -ForegroundColor Cyan
Write-Host "If all tests passed, you can now run the Android app!" -ForegroundColor Green
