@echo off
REM Batch script to test Android emulator connection to host

echo.
echo ========================================
echo Android Emulator Network Test
echo ========================================
echo.

REM Check if adb is available
where adb >nul 2>nul
if %errorlevel% neq 0 (
    echo ERROR: adb not found in PATH
    echo Make sure Android SDK is installed and adb is in your PATH
    echo.
    echo Add this to your PATH:
    echo C:\Users\alely\AppData\Local\Android\Sdk\platform-tools
    pause
    exit /b 1
)

echo [1/4] Checking if emulator is running...
adb devices
echo.

echo [2/4] Pinging 10.0.2.2 from emulator...
adb shell ping -c 4 10.0.2.2
if %errorlevel% neq 0 (
    echo.
    echo WARNING: Ping failed! Emulator cannot reach host.
    echo.
    echo Try these fixes:
    echo 1. Restart emulator: adb emu kill
    echo 2. Check emulator type (must be Android Emulator, not Google Play)
    echo 3. Check Windows Firewall allows port 80
    echo.
) else (
    echo.
    echo SUCCESS: Emulator can reach host!
    echo.
)

echo [3/4] Testing HTTP connection from emulator...
adb shell curl -v http://10.0.2.2/scheduling-api/test.php 2>nul
if %errorlevel% neq 0 (
    echo.
    echo Note: curl may not be available in emulator
    echo Trying telnet instead...
    adb shell telnet 10.0.2.2 80
)

echo.
echo [4/4] Checking XAMPP on host...
curl http://localhost/scheduling-api/test.php 2>nul
if %errorlevel% neq 0 (
    echo.
    echo ERROR: Cannot reach XAMPP on host
    echo Make sure XAMPP Apache is running
) else (
    echo.
    echo SUCCESS: XAMPP is running!
)

echo.
echo ========================================
echo Test Complete
echo ========================================
echo.
echo If all tests passed, run your Android app!
echo.
pause
