# Exact Steps to Fix Emulator Network Connection

## The Mistake You Made

You ran:
```powershell
ping -n 4 10.0.2.2
```

This pings from **your Windows PC**, not from the **Android emulator**.

The 10.0.2.2 address **only works inside the emulator**. Your PC cannot reach it.

## The Correct Way

### Step 1: Open Android Studio Terminal

In Android Studio:
- **View** → **Tool Windows** → **Terminal**
- Or press `Alt + F12`

You should see a terminal at the bottom of Android Studio.

### Step 2: Run This Command

```bash
adb shell ping -c 4 10.0.2.2
```

This runs ping **inside the emulator**, not on your PC.

### Step 3: Check the Result

**If you see this (SUCCESS):**
```
PING 10.0.2.2 (10.0.2.2) 56(84) bytes of data.
64 bytes from 10.0.2.2: icmp_seq=1 ttl=63 time=0.123 ms
64 bytes from 10.0.2.2: icmp_seq=2 ttl=63 time=0.156 ms
64 bytes from 10.0.2.2: icmp_seq=3 ttl=63 time=0.145 ms
64 bytes from 10.0.2.2: icmp_seq=4 ttl=63 time=0.134 ms
```

✅ **Your emulator can reach the host!**

Go to Step 5 (Run App).

**If you see this (FAILURE):**
```
PING 10.0.2.2 (10.0.2.2) 56(84) bytes of data.
Request timed out.
Request timed out.
Request timed out.
Request timed out.
```

❌ **Emulator cannot reach host.**

Go to Step 4 (Fix Network).

## Step 4: Fix Network (If Ping Failed)

### Option A: Restart Emulator (Try This First)

In Android Studio Terminal:
```bash
adb emu kill
```

Wait 5 seconds, then:
1. Click the green **Run** button in Android Studio
2. Select your emulator
3. App will start

Then test again:
```bash
adb shell ping -c 4 10.0.2.2
```

### Option B: Check Emulator Type (If Restart Didn't Work)

1. **Android Studio** → **AVD Manager**
2. Look at your emulator
3. Check the **Release Name** column

**If it says "Google Play":**
- ❌ This emulator doesn't support 10.0.2.2
- You must create a new one without Google Play

**To create new emulator:**
1. AVD Manager → **Create Virtual Device**
2. Select **Pixel 4** (or any device)
3. Select **API 33** (or any API level)
4. Click **Next**
5. **UNCHECK "Google Play"** ← IMPORTANT
6. Click **Finish**
7. Use this new emulator

### Option C: Check Windows Firewall

1. **Windows Defender Firewall** → **Advanced Settings**
2. **Inbound Rules** → **New Rule**
3. Select **Port** → **Next**
4. Select **TCP** → **Specific local ports: 80** → **Next**
5. Select **Allow the connection** → **Next**
6. Check all three: Domain, Private, Public → **Next**
7. **Name**: Apache HTTP → **Finish**

Then restart Apache in XAMPP.

### Option D: Configure XAMPP

Edit this file:
```
C:\xampp\apache\conf\httpd.conf
```

Find this line:
```apache
Listen 80
```

Change to:
```apache
Listen 0.0.0.0:80
```

Save and restart Apache in XAMPP.

## Step 5: Run Your App

Once `adb shell ping -c 4 10.0.2.2` works:

1. **Android Studio** → Click green **Run** button
2. Select your emulator
3. App starts
4. Watch **Logcat** for messages:
   - Look for filter: `LoginActivity`
   - Should see: `✓ Test Connection - Code: 200`

If you see that message, the connection works!

## Step 6: Test Login

1. Enter email: `admin@example.com`
2. Enter password: `password123`
3. Click **Sign In**
4. Should see: `Welcome, John Michael Doe!`

## Quick Reference Commands

```bash
# In Android Studio Terminal:

# Check if emulator is running
adb devices

# Ping from emulator to host (THE CORRECT WAY)
adb shell ping -c 4 10.0.2.2

# Test HTTP from emulator
adb shell curl http://10.0.2.2/scheduling-api/test.php

# Restart emulator
adb emu kill

# Check XAMPP on your PC (from Windows PowerShell)
curl http://localhost/scheduling-api/test.php
```

## Common Mistakes

| Mistake | Problem | Fix |
|---------|---------|-----|
| `ping 10.0.2.2` (from PC) | Pings from Windows, not emulator | Use `adb shell ping -c 4 10.0.2.2` |
| Using Google Play emulator | Doesn't support 10.0.2.2 | Create standard Android Emulator |
| XAMPP not running | Backend not accessible | Start Apache in XAMPP |
| Firewall blocking | Port 80 blocked | Add firewall exception for port 80 |

## Troubleshooting Flowchart

```
1. Run: adb shell ping -c 4 10.0.2.2
   ↓
   Success? → Go to Step 5 (Run App)
   ↓
   Failure? → Continue...
   ↓
2. Run: adb emu kill (restart emulator)
   ↓
   Try ping again
   ↓
   Success? → Go to Step 5 (Run App)
   ↓
   Failure? → Continue...
   ↓
3. Check emulator type (must be Android Emulator, not Google Play)
   ↓
   Using Google Play? → Create new emulator without it
   ↓
   Try ping again
   ↓
   Success? → Go to Step 5 (Run App)
   ↓
   Failure? → Continue...
   ↓
4. Check Windows Firewall (add port 80 exception)
   ↓
5. Check XAMPP config (Listen 0.0.0.0:80)
   ↓
6. Restart everything and try again
```

## Success Indicators

✅ You'll know it's working when:
- `adb shell ping -c 4 10.0.2.2` returns 4 successful pings
- Logcat shows: `✓ Test Connection - Code: 200`
- Login works with `admin@example.com` / `password123`
- You see: `Welcome, John Michael Doe!`

## Need Help?

If still stuck:
1. Run: `adb shell curl http://10.0.2.2/scheduling-api/test.php`
2. Check output in Android Studio Terminal
3. If error, check XAMPP is running: `http://localhost/scheduling-api/test.php` in browser

The backend works (your curl command proved it). Just need to fix emulator network!
