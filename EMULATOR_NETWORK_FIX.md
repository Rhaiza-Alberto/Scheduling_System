# Emulator Network Connection Fix

## Problem
Error: `Failed to connect to /10.0.2.2:80` - `ENETUNREACH (Network is unreachable)`

The emulator cannot reach your host machine's localhost.

## Root Causes

1. **Using Google Play emulator** - Doesn't support 10.0.2.2
2. **Emulator network settings** - Not configured correctly
3. **Firewall blocking** - Windows firewall blocking port 80
4. **XAMPP not accessible** - Apache not listening on all interfaces

## Solutions

### Solution 1: Use Android Studio Emulator (Recommended)

1. **Open Android Studio**
2. **AVD Manager** → Click the emulator you're using
3. **Check the emulator type**:
   - ✅ **Good**: "Android Emulator" (uses QEMU)
   - ❌ **Bad**: "Google Play" (doesn't support 10.0.2.2)

If using Google Play emulator, create a new standard Android Emulator:
1. AVD Manager → Create Virtual Device
2. Select a device (e.g., Pixel 4)
3. Select API level (e.g., API 33)
4. **Uncheck "Google Play"** 
5. Finish and use this emulator

### Solution 2: Configure XAMPP to Listen on All Interfaces

Edit `C:\xampp\apache\conf\httpd.conf`:

Find:
```apache
Listen 80
```

Change to:
```apache
Listen 0.0.0.0:80
```

Or find the VirtualHost section and ensure it's:
```apache
<VirtualHost *:80>
```

Then restart Apache.

### Solution 3: Windows Firewall Exception

1. **Windows Defender Firewall** → Advanced Settings
2. **Inbound Rules** → New Rule
3. **Port** → Next
4. **TCP** → Specific local ports: `80`
5. **Allow the connection** → Next
6. **Apply to**: Domain, Private, Public
7. **Name**: "Apache HTTP"
8. **Finish**

### Solution 4: Test Emulator Network

Run this in Android Studio Terminal:

```bash
# Check if 10.0.2.2 is reachable
adb shell ping -c 4 10.0.2.2

# Expected output:
# PING 10.0.2.2 (10.0.2.2) 56(84) bytes of data.
# 64 bytes from 10.0.2.2: icmp_seq=1 ttl=63 time=0.123 ms
```

If ping fails, the emulator cannot reach the host.

### Solution 5: Alternative - Use Physical Device IP

If emulator still doesn't work, use a physical Android device:

1. **Find your PC IP**:
   ```powershell
   ipconfig
   # Look for "IPv4 Address" (e.g., 192.168.1.100)
   ```

2. **Update LoginActivity.kt**:
   ```kotlin
   // Replace 10.0.2.2 with your actual IP
   .url("http://192.168.1.100/scheduling-api/login.php")
   ```

3. **Ensure device is on same network** as your PC

4. **Test connectivity**:
   ```bash
   # From device terminal
   ping 192.168.1.100
   ```

## Quick Checklist

- [ ] Using Android Emulator (not Google Play)
- [ ] XAMPP Apache is running
- [ ] Port 80 is open in Windows Firewall
- [ ] `http://localhost/scheduling-api/test.php` works in browser
- [ ] Emulator can ping 10.0.2.2: `adb shell ping -c 4 10.0.2.2`
- [ ] LoginActivity shows "Backend connected!" toast

## Testing Steps

### Step 1: Verify XAMPP
```bash
# In browser
http://localhost/scheduling-api/test.php
# Should return JSON
```

### Step 2: Test Emulator Network
```bash
# In Android Studio Terminal
adb shell ping -c 4 10.0.2.2
```

### Step 3: Run App
1. Start emulator
2. Run app
3. Watch Logcat for connection messages

### Step 4: Check Logcat
```
✓ Test Connection - Code: 200
✓ Response: {"success":true,...}
```

## If Still Not Working

Try this diagnostic script in Android Studio Terminal:

```bash
# Check emulator connectivity
adb shell curl -v http://10.0.2.2/scheduling-api/test.php

# If curl not available, use telnet
adb shell telnet 10.0.2.2 80
```

## Network Diagram

```
┌─────────────────────────────────────┐
│         Your PC (Windows)           │
│  ┌─────────────────────────────────┐│
│  │  XAMPP (Apache on port 80)      ││
│  │  http://localhost:80            ││
│  └─────────────────────────────────┘│
│           ↑                          │
│           │ (10.0.2.2 special IP)   │
│           │                          │
│  ┌─────────────────────────────────┐│
│  │   Android Emulator              ││
│  │   (QEMU-based)                  ││
│  │   App tries to connect to       ││
│  │   http://10.0.2.2/...           ││
│  └─────────────────────────────────┘│
└─────────────────────────────────────┘
```

## Important Notes

- **10.0.2.2** only works with standard Android Emulator (QEMU)
- **Google Play emulator** requires physical device IP or different approach
- **Firewall** must allow port 80 traffic
- **XAMPP** must be listening on all interfaces (0.0.0.0:80)

## Next Steps

1. Verify you're using standard Android Emulator
2. Check Windows Firewall allows port 80
3. Test: `adb shell ping -c 4 10.0.2.2`
4. If ping works, run the app again
5. Check Logcat for connection status
