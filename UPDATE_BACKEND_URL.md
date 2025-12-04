# Update Backend URL - Quick Guide

## Your Emulator Cannot Reach 10.0.2.2

The emulator's network is isolated. You need to use your actual PC IP instead.

## Step 1: Get Your PC IP

In PowerShell:
```powershell
ipconfig
```

Look for:
```
IPv4 Address. . . . . . . . . . . : 192.168.1.100
```

**Copy this IP** (e.g., `192.168.1.100`)

## Step 2: Update LoginActivity.kt

Open: `app/src/main/java/com/example/schedulingSystem/LoginActivity.kt`

Find line 33:
```kotlin
private const val BACKEND_URL = "http://10.0.2.2/scheduling-api"
```

Replace with your PC IP:
```kotlin
private const val BACKEND_URL = "http://192.168.1.100/scheduling-api"
```

**Replace `192.168.1.100` with YOUR actual IP from ipconfig!**

## Step 3: Save and Run

1. Save the file (Ctrl + S)
2. Click **Run** in Android Studio
3. App should connect to backend

## Step 4: Test

1. App should show: `✓ Backend connected!` toast
2. Enter: `admin@example.com` / `password123`
3. Click Sign In
4. Should see: `Welcome, John Michael Doe!`

## Example IPs

| Scenario | IP | Code |
|----------|----|----|
| Emulator (if working) | 10.0.2.2 | `http://10.0.2.2/scheduling-api` |
| Physical device on WiFi | 192.168.1.100 | `http://192.168.1.100/scheduling-api` |
| Physical device on different network | Your PC IP | `http://YOUR_IP/scheduling-api` |

## Verify XAMPP is Running

Before testing, make sure XAMPP is running:

In browser:
```
http://localhost/scheduling-api/test.php
```

Should return JSON.

## If Still Not Working

1. Check XAMPP is running
2. Verify correct IP from ipconfig
3. Check Windows Firewall allows port 80
4. Try physical device instead of emulator

## Quick Reference

**File to edit:** `LoginActivity.kt` (line 33)

**What to change:**
- From: `http://10.0.2.2/scheduling-api`
- To: `http://YOUR_PC_IP/scheduling-api`

**Your PC IP:** Run `ipconfig` and look for IPv4 Address

That's it! Just one line to change.
