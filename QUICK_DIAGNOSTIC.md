# Quick Diagnostic Checklist

## Your Error
```
Failed to connect to /10.0.2.2:80
ENETUNREACH (Network is unreachable)
```

This means the **emulator cannot reach your host machine**.

## Quick Fix Steps (Do These First)

### 1. Check Emulator Type (CRITICAL)
```
Android Studio → AVD Manager → Look at your emulator
```

**What you should see:**
- ✅ "Android Emulator" (Good - uses QEMU)
- ❌ "Google Play" (Bad - doesn't support 10.0.2.2)

**If using Google Play:**
1. Create new emulator without Google Play
2. Use that instead

### 2. Test Emulator Network (In Android Studio Terminal)
```bash
adb shell ping -c 4 10.0.2.2
```

**Expected output:**
```
PING 10.0.2.2 (10.0.2.2) 56(84) bytes of data.
64 bytes from 10.0.2.2: icmp_seq=1 ttl=63 time=0.123 ms
```

**If it fails:**
- Emulator cannot reach host
- Need to fix network settings (see below)

### 3. Verify XAMPP is Running
```
Browser: http://localhost/scheduling-api/test.php
```

Should return JSON. If not:
- Start Apache in XAMPP
- Check it's listening on port 80

### 4. Check Windows Firewall
```
Windows Defender Firewall → Advanced Settings → Inbound Rules
```

Look for Apache or port 80 rule. If missing:
1. New Rule → Port → TCP → 80
2. Allow connection
3. Apply to all (Domain, Private, Public)

### 5. Run App Again
1. Close and restart emulator
2. Run app
3. Watch Logcat for connection status

## Detailed Troubleshooting

### If Emulator Ping Fails

**Option A: Restart Emulator**
```bash
adb emu kill
# Then restart from Android Studio
```

**Option B: Wipe Emulator Data**
```bash
adb emu kill
# Android Studio → AVD Manager → Wipe Data
# Then restart
```

**Option C: Use Physical Device**
1. Connect Android phone via USB
2. Find your PC IP: `ipconfig` → IPv4 Address (e.g., 192.168.1.100)
3. Update LoginActivity.kt:
   ```kotlin
   .url("http://192.168.1.100/scheduling-api/login.php")
   ```
4. Ensure phone is on same WiFi network

### If XAMPP Not Accessible

**Edit Apache config:**
```
C:\xampp\apache\conf\httpd.conf
```

Find:
```apache
Listen 80
```

Change to:
```apache
Listen 0.0.0.0:80
```

Restart Apache.

### If Firewall Blocking

**Add Exception:**
1. Windows Defender Firewall → Advanced Settings
2. Inbound Rules → New Rule
3. Port → TCP → 80
4. Allow → Domain, Private, Public
5. Name: "Apache HTTP"

## Logcat Messages to Look For

**Good signs:**
```
✓ Test Connection - Code: 200
✓ Response: {"success":true,...}
→ Sending login request for: admin@example.com
← Response Code: 200
```

**Bad signs:**
```
✗ Test Connection Failed: Network is unreachable
✗ Exception type: ConnectException
✗ Caused by: ENETUNREACH
```

## One-Minute Test

```bash
# Terminal 1: Check emulator can reach host
adb shell ping -c 4 10.0.2.2

# Terminal 2: Check XAMPP is running
curl http://localhost/scheduling-api/test.php

# Terminal 3: Run app and watch Logcat
# Filter: LoginActivity
```

If all three work, the app should connect!

## Common Issues & Fixes

| Error | Cause | Fix |
|-------|-------|-----|
| ENETUNREACH | Emulator can't reach host | Restart emulator, check firewall |
| Connection refused | XAMPP not running | Start Apache in XAMPP |
| Network unreachable | Firewall blocking | Add port 80 exception |
| Using Google Play emulator | Wrong emulator type | Create standard Android Emulator |

## Still Not Working?

Try this in Android Studio Terminal:
```bash
# Test from emulator itself
adb shell curl -v http://10.0.2.2/scheduling-api/test.php

# Or use telnet
adb shell telnet 10.0.2.2 80
```

If these fail, the emulator network is misconfigured.

## Next Steps

1. ✅ Check emulator type (Android Emulator, not Google Play)
2. ✅ Test: `adb shell ping -c 4 10.0.2.2`
3. ✅ Verify XAMPP running: `http://localhost/scheduling-api/test.php`
4. ✅ Check Windows Firewall allows port 80
5. ✅ Restart emulator and run app
6. ✅ Watch Logcat for connection messages

The backend is working (curl works!) - just need to fix emulator network!
