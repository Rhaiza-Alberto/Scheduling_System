# PowerShell script to fix emulator network issues

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Android Emulator Network Fix" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$adbPath = "C:\Users\alely\AppData\Local\Android\Sdk\platform-tools\adb.exe"

# Check if adb exists
if (-not (Test-Path $adbPath)) {
    Write-Host "ERROR: adb not found" -ForegroundColor Red
    exit 1
}

# Menu
Write-Host "Choose an option:" -ForegroundColor Yellow
Write-Host ""
Write-Host "1. Restart emulator (usually fixes network issues)" -ForegroundColor Green
Write-Host "2. Check emulator network status" -ForegroundColor Green
Write-Host "3. Test connectivity" -ForegroundColor Green
Write-Host "4. Add Windows Firewall exception for port 80" -ForegroundColor Green
Write-Host "5. Exit" -ForegroundColor Green
Write-Host ""

$choice = Read-Host "Enter your choice (1-5)"

switch ($choice) {
    "1" {
        Write-Host ""
        Write-Host "Restarting emulator..." -ForegroundColor Yellow
        & $adbPath emu kill
        Write-Host "Emulator killed. Waiting 5 seconds..." -ForegroundColor Yellow
        Start-Sleep -Seconds 5
        Write-Host "Now run your app from Android Studio to restart the emulator." -ForegroundColor Green
        Write-Host "After emulator starts, run this script again and choose option 3." -ForegroundColor Green
    }
    
    "2" {
        Write-Host ""
        Write-Host "Checking emulator network status..." -ForegroundColor Yellow
        Write-Host ""
        
        Write-Host "Network interfaces:" -ForegroundColor Cyan
        & $adbPath shell ifconfig
        Write-Host ""
        
        Write-Host "Routing table:" -ForegroundColor Cyan
        & $adbPath shell route
    }
    
    "3" {
        Write-Host ""
        Write-Host "Testing connectivity..." -ForegroundColor Yellow
        Write-Host ""
        
        Write-Host "[1/3] Pinging 127.0.0.1 (localhost)..." -ForegroundColor Cyan
        & $adbPath shell ping -c 2 127.0.0.1
        Write-Host ""
        
        Write-Host "[2/3] Pinging 8.8.8.8 (Google DNS)..." -ForegroundColor Cyan
        & $adbPath shell ping -c 2 8.8.8.8
        Write-Host ""
        
        Write-Host "[3/3] Pinging 10.0.2.2 (host machine)..." -ForegroundColor Cyan
        & $adbPath shell ping -c 4 10.0.2.2
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host ""
            Write-Host "✓ SUCCESS! Emulator can reach host!" -ForegroundColor Green
        } else {
            Write-Host ""
            Write-Host "✗ FAILED! Emulator cannot reach host." -ForegroundColor Red
            Write-Host ""
            Write-Host "Try these fixes:" -ForegroundColor Yellow
            Write-Host "1. Restart emulator (option 1)" -ForegroundColor Yellow
            Write-Host "2. Check Windows Firewall (option 4)" -ForegroundColor Yellow
            Write-Host "3. Wipe emulator data in Android Studio" -ForegroundColor Yellow
        }
    }
    
    "4" {
        Write-Host ""
        Write-Host "Adding Windows Firewall exception for port 80..." -ForegroundColor Yellow
        
        # Check if running as admin
        $isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole] "Administrator")
        
        if (-not $isAdmin) {
            Write-Host "ERROR: This requires Administrator privileges" -ForegroundColor Red
            Write-Host "Please run PowerShell as Administrator" -ForegroundColor Yellow
            exit 1
        }
        
        try {
            New-NetFirewallRule -DisplayName "Allow HTTP Port 80" `
                -Direction Inbound `
                -Action Allow `
                -Protocol TCP `
                -LocalPort 80 `
                -ErrorAction Stop
            
            Write-Host "✓ Firewall rule added successfully!" -ForegroundColor Green
            Write-Host "Restart Apache in XAMPP for changes to take effect." -ForegroundColor Yellow
        }
        catch {
            Write-Host "✗ Failed to add firewall rule: $_" -ForegroundColor Red
        }
    }
    
    "5" {
        Write-Host "Exiting..." -ForegroundColor Yellow
        exit 0
    }
    
    default {
        Write-Host "Invalid choice. Exiting." -ForegroundColor Red
        exit 1
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Done" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
