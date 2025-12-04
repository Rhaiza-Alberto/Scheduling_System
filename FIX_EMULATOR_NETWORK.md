# Fix Emulator Network Connection

## The Issue You're Facing

You pinged from **Windows PowerShell** (your PC), not from the **Android emulator**. That's why it timed out.

The 10.0.2.2 address is **special** - it only works **inside the Android emulator** to reach the host machine. Your PC cannot ping 10.0.2.2 directly.

## Correct Way to Test

### Step 1: Open Android Studio Terminal

In Android Studio:
1. **View** → **Tool Windows** → **Terminal**
2. Or press `Alt + F12`

### Step 2: Ping from Emulator

```bash
adb shell ping -c 4 10.0.2.2
```

This runs the ping command **inside the emulator**, not on your PC.

**Expected output (if working):**
```
PING 10.0.2.2 (10.0.2.2) 56(84) bytes of data.
64 bytes from 10.0.2.2: icmp_seq=1 ttl=63 time=0.123 ms
64 bytes from 10.0.2.2: icmp_seq=2 ttl=63 time=0.156 ms
64 bytes from 10.0.2.2: icmp_seq=3 ttl=63 time=0.145 ms
64 bytes from 10.0.2.2: icmp_seq=4 ttl=63 time=0.134 ms
```

**If you get timeouts:**
```
PING 10.0.2.2 (10.0.2.2) 56(84) bytes of data.
Request timed out.
Request timed out.
```

## If Emulator Ping Fails

### Option 1: Restart Emulator (Usually Fixes It)

```bash
# Kill emulator
adb emu kill

# Wait a few seconds, then restart from Android Studio
# Click the green play button to run your app
```

### Option 2: Wipe Emulator Data

```bash
# Kill emulator
adb emu kill

# In Android Studio:
# AVD Manager → Right-click emulator → Wipe Data
# Then restart
```

### Option 3: Check Emulator Type

**This is CRITICAL:**

1. **Android Studio** → **AVD Manager**
2. Look at your emulator in the list
3. Check the **Release Name** column:
   - ✅ **Good**: Shows "Android X.X" (e.g., "Android 13")
   - ❌ **Bad**: Shows "Google Play" or has a "Play" badge

**If using Google Play emulator:**
- It doesn't support 10.0.2.2
- You must create a **standard Android Emulator** without Google Play
- Steps:
  1. AVD Manager → **Create Virtual Device**
  2. Select device (e.g., Pixel 4)
  3. Select API level (e.g., API 33)
  4. **Uncheck "Google Play"** ← IMPORTANT
  5. Finish
  6. Use this new emulator

### Option 4: Check Windows Firewall

Windows Firewall might be blocking the connection:

1. **Windows Defender Firewall** → **Advanced Settings**
2. **Inbound Rules** → **New Rule**
3. **Rule Type**: Port
4. **Protocol**: TCP
5. **Specific local ports**: 80
6. **Action**: Allow the connection
7. **When does it apply**: Domain, Private, Public
8. **Name**: Apache HTTP
9. **Finish**

Then restart Apache in XAMPP.

### Option 5: Configure XAMPP to Listen on All Interfaces

Edit `C:\xampp\apache\conf\httpd.conf`:

```bash
# Find this line:
Listen 80

# Change to:
Listen 0.0.0.0:80
```

Save and restart Apache.

## Complete Diagnostic Steps

Run these commands in **Android Studio Terminal** (one at a time):

```bash
# 1. Check if emulator is running
adb devices

# Expected: Should show your emulator as "device" (not "offline")

# 2. Ping from emulator to host
adb shell ping -c 4 10.0.2.2

# Expected: 4 successful pings

# 3. Test HTTP from emulator
adb shell curl -v http://10.0.2.2/scheduling-api/test.php

# Expected: HTTP 200 response with JSON

# 4. If curl not available, use telnet
adb shell telnet 10.0.2.2 80

# Expected: Connected message
```

## Quick Checklist

- [ ] Using **Android Emulator** (not Google Play)
- [ ] Emulator is **running** (check AVD Manager)
- [ ] `adb devices` shows emulator as "device"
- [ ] `adb shell ping -c 4 10.0.2.2` succeeds
- [ ] XAMPP Apache is **running**
- [ ] Windows Firewall allows **port 80**
- [ ] `http://localhost/scheduling-api/test.php` works in browser

## Network Diagram

```
┌──────────────────────────────────────────┐
│         Your Windows PC                  │
│  ┌────────────────────────────────────┐  │
│  │  XAMPP (Apache on port 80)         │  │
│  │  http://localhost:80               │  │
│  └────────────────────────────────────┘  │
│           ↑                               │
│           │ (10.0.2.2 - special IP)      │
│           │ (only works from emulator)   │
│  ┌────────────────────────────────────┐  │
│  │  Android Emulator (QEMU)           │  │
│  │  Can reach host via 10.0.2.2       │  │
│  │  Your app connects here            │  │
│  └────────────────────────────────────┘  │
└──────────────────────────────────────────┘

Your PC cannot ping 10.0.2.2 directly!
Only the emulator can reach it.
```

## Important Notes

- **10.0.2.2** is a **special address** that only works inside the Android emulator
- Your PC cannot ping it (that's normal and expected)
- You must use `adb shell` commands to test from the emulator
- If emulator ping fails, restart the emulator (usually fixes it)
- If using Google Play emulator, you MUST create a standard emulator

## After Fixing Network

Once `adb shell ping -c 4 10.0.2.2` works:

1. Run your Android app
2. Watch Logcat for: `✓ Test Connection - Code: 200`
3. Try logging in with: `admin@example.com` / `password123`
4. Should see: `Welcome, John Michael Doe!`

## Still Having Issues?

Try this command in Android Studio Terminal:

```bash
# Restart emulator completely
adb emu kill
# Wait 5 seconds
# Then run your app again from Android Studio
```

This usually fixes network connectivity issues.
