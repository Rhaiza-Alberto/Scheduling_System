# PowerShell script to test the fixed login endpoint

Write-Host "=== Testing Fixed Login Endpoint ===" -ForegroundColor Cyan
Write-Host ""

# Test login with correct credentials
Write-Host "Testing login with admin@example.com..." -ForegroundColor Yellow

try {
    $body = @{
        username = "admin@example.com"
        password = "password123"
    } | ConvertTo-Json
    
    Write-Host "Request body: $body" -ForegroundColor Gray
    
    $response = Invoke-WebRequest -Uri "http://localhost/scheduling-api/login.php" `
        -Method Post `
        -ContentType "application/json" `
        -Body $body `
        -TimeoutSec 5
    
    $content = $response.Content | ConvertFrom-Json
    
    Write-Host ""
    Write-Host "Response Status: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "Response Body:" -ForegroundColor Green
    Write-Host ($content | ConvertTo-Json -Depth 10) -ForegroundColor Green
    
    if ($content.success) {
        Write-Host ""
        Write-Host "✓ LOGIN SUCCESSFUL!" -ForegroundColor Green
        Write-Host "  User: $($content.user.name)" -ForegroundColor Green
        Write-Host "  Role: $($content.user.account_type)" -ForegroundColor Green
        Write-Host "  ID: $($content.user.person_ID)" -ForegroundColor Green
    } else {
        Write-Host ""
        Write-Host "✗ Login failed: $($content.message)" -ForegroundColor Red
    }
} catch {
    Write-Host "✗ Request failed" -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
    Write-Host "Make sure:" -ForegroundColor Yellow
    Write-Host "  1. XAMPP is running" -ForegroundColor Yellow
    Write-Host "  2. Database 'scheduling-system' exists" -ForegroundColor Yellow
    Write-Host "  3. Test data has been inserted" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "=== Test Complete ===" -ForegroundColor Cyan
