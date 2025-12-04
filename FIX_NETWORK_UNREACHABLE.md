# Fix "Network is unreachable" Error

## Your Current Status

✅ adb is working
✅ Emulator is running (emulator-5554)
❌ Emulator cannot reach host (10.0.2.2)

Error: `connect: Network is unreachable`

## Root Cause

The Android emulator's network is not properly configured to reach the host machine.

## Solutions (Try in Order)

### Solution 1: Restart Emulator (Usually Fixes It)

```powershell
# Kill the emulator
adb emu kill

# Wait 5 seconds
Start-Sleep -Seconds 5

# In Android Studio: Click the green Run button to restart the app
# This will restart the emulator with proper network settings
```

Then test again:
```powershell
adb shell ping -c 4 10.0.2.2
```

### Solution 2: Wipe Emulator Data

```powershell
# Kill emulator
adb emu kill

# In Android Studio:
# 1. AVD Manager (Tools → Device Manager)
# 2. Right-click your emulator
# 3. Click "Wipe Data"
# 4. Click Run to restart
```

Then test:
```powershell
adb shell ping -c 4 10.0.2.2
```

### Solution 3: Check Emulator Network Settings

In Android Studio:
1. **Tools** → **Device Manager** (or **AVD Manager**)
2. Right-click your emulator → **Edit**
3. Click **Show Advanced Settings**
4. Look for **Network** section:
   - **IPv4 address**: Should be auto-configured
   - **DNS servers**: Should have values (e.g., 8.8.8.8)
5. If empty, set:
   - **IPv4 address**: Leave as auto
   - **DNS servers**: 8.8.8.8, 8.8.4.4
6. Click **Finish**
7. Restart emulator

### Solution 4: Check Windows Firewall

Windows Firewall might be blocking the connection:

```powershell
# Run as Administrator
# Add exception for port 80
New-NetFirewallRule -DisplayName "Allow HTTP" -Direction Inbound -Action Allow -Protocol TCP -LocalPort 80
```

Or manually:
1. **Windows Defender Firewall** → **Advanced Settings**
2. **Inbound Rules** → **New Rule**
3. **Port** → **TCP** → **80**
4. **Allow** → **Domain, Private, Public**
5. **Name**: Apache HTTP
6. **Finish**

Then restart Apache in XAMPP.

### Solution 5: Configure XAMPP to Listen on All Interfaces

Edit: `C:\xampp\apache\conf\httpd.conf`

Find:
```apache
Listen 80
```

Change to:
```apache
Listen 0.0.0.0:80
```

Save and restart Apache.

### Solution 6: Use Different Emulator

If you're using **Google Play emulator**, it doesn't support 10.0.2.2:

1. **Android Studio** → **Device Manager**
2. Check your emulator's **Release Name**
3. If it says "Google Play", create a new one:
   - **Create Virtual Device**
   - Select **Pixel 4**
   - Select **API 33**
   - **Uncheck "Google Play"** ← IMPORTANT
   - **Finish**
4. Use this new emulator

### Solution 7: Use Physical Device Instead

If emulator still doesn't work, use a physical Android device:

1. Connect phone via USB
2. Enable USB Debugging on phone
3. Find your PC IP:
   ```powershell
   ipconfig
   # Look for IPv4 Address (e.g., 192.168.1.100)
   ```
4. Update LoginActivity.kt:
   ```kotlin
   .url("http://192.168.1.100/scheduling-api/login.php")
   ```
5. Make sure phone is on same WiFi network
6. Test:
   ```powershell
   adb devices
   # Should show your phone
   ```

## Diagnostic Commands

Run these to understand the issue:

```powershell
# Check emulator network info
adb shell ifconfig

# Check routing
adb shell route

# Try to reach localhost
adb shell ping -c 4 127.0.0.1

# Try to reach 8.8.8.8 (Google DNS)
adb shell ping -c 4 8.8.8.8

# Check if telnet works
adb shell telnet 10.0.2.2 80
```

## Expected vs Actual

**Expected (working):**
```
PING 10.0.2.2 (10.0.2.2) 56(84) bytes of data.
64 bytes from 10.0.2.2: icmp_seq=1 ttl=63 time=0.123 ms
```

**Actual (not working):**
```
connect: Network is unreachable
```

## Quick Checklist

- [ ] Restart emulator: `adb emu kill`
- [ ] Check emulator type (not Google Play)
- [ ] Verify XAMPP is running
- [ ] Check Windows Firewall allows port 80
- [ ] Configure XAMPP to listen on 0.0.0.0:80
- [ ] Wipe emulator data and restart
- [ ] Test again: `adb shell ping -c 4 10.0.2.2`

## Recommended Order

1. **Try Solution 1** (Restart) - 30 seconds
2. **Try Solution 2** (Wipe Data) - 1 minute
3. **Try Solution 3** (Network Settings) - 2 minutes
4. **Try Solution 4** (Firewall) - 2 minutes
5. **Try Solution 5** (XAMPP Config) - 1 minute
6. **Try Solution 6** (Different Emulator) - 5 minutes
7. **Try Solution 7** (Physical Device) - 10 minutes

## If Still Not Working

Try these diagnostic commands:

```powershell
# Check if emulator can reach anything
adb shell ping -c 4 127.0.0.1
# If this fails, emulator network is completely broken

# Check emulator network config
adb shell ifconfig
# Look for "inet addr" - should have an IP like 10.0.2.15

# Check if XAMPP is actually running
curl http://localhost/scheduling-api/test.php
# Should return JSON
```

## Network Diagram

```
┌────────────────────────────────────┐
│    Your Windows PC                 │
│  ┌──────────────────────────────┐  │
│  │  XAMPP (Apache)              │  │
│  │  Listening on 0.0.0.0:80     │  │
│  └──────────────────────────────┘  │
│           ↑                         │
│           │ (10.0.2.2)             │
│           │ (BLOCKED - FIX THIS)   │
│  ┌──────────────────────────────┐  │
│  │  Android Emulator            │  │
│  │  Cannot reach host           │  │
│  └──────────────────────────────┘  │
└────────────────────────────────────┘
```

## Important Notes

- 10.0.2.2 is a special address that only works in Android emulator
- Network must be properly configured for emulator to use it
- Restarting emulator often fixes temporary network issues
- Windows Firewall can block the connection
- XAMPP must listen on all interfaces (0.0.0.0:80)

## Next Steps

1. Try **Solution 1** (restart emulator)
2. Run: `adb shell ping -c 4 10.0.2.2`
3. If still fails, try **Solution 2** (wipe data)
4. If still fails, try **Solution 4** (firewall)
5. Let me know the results!
