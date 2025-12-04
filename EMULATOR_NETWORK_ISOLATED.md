# Emulator Network is Isolated - Fix Guide

## Your Current Situation

```
✅ Emulator can reach: 127.0.0.1 (localhost)
❌ Emulator cannot reach: 8.8.8.8 (internet)
❌ Emulator cannot reach: 10.0.2.2 (host machine)
```

**Problem:** The emulator's network is isolated and cannot reach external networks.

## Root Cause

The Android emulator is running in **isolated network mode**. This happens when:
1. Emulator network settings are misconfigured
2. Using wrong emulator type (Google Play vs standard)
3. Network adapter issues
4. Emulator was created with network disabled

## Solution: Recreate Emulator with Proper Network

### Step 1: Delete Current Emulator

In **Android Studio**:
1. **Tools** → **Device Manager**
2. Right-click your emulator (emulator-5554)
3. Click **Delete**
4. Confirm deletion

### Step 2: Create New Emulator with Network Enabled

1. **Device Manager** → **Create Virtual Device**
2. Select device: **Pixel 4** (or any device)
3. Select API level: **API 33** (or higher)
4. Click **Next**
5. Click **Show Advanced Settings**
6. Look for **Network** section:
   - **Front camera**: Default
   - **Back camera**: Default
   - **Boot option**: Cold boot
   - **Network speed**: Full
   - **Network latency**: None
7. Scroll down to find **Network settings**:
   - Make sure network is **enabled** (not disabled)
8. Click **Finish**

### Step 3: Verify Emulator Type

In **Device Manager**, check your new emulator:
- Should show: "Android X.X" (e.g., "Android 13")
- Should NOT show: "Google Play" badge

### Step 4: Test Network

Run your app from Android Studio. Then test:

```powershell
adb shell ping -c 4 127.0.0.1
# Should work

adb shell ping -c 4 8.8.8.8
# Should work

adb shell ping -c 4 10.0.2.2
# Should work
```

## Alternative Solution: Use Physical Device

If recreating emulator doesn't work, use your Android phone:

### Step 1: Connect Phone

1. Connect Android phone via USB cable
2. Enable **USB Debugging** on phone:
   - Settings → Developer Options → USB Debugging
3. Trust the computer when prompted

### Step 2: Verify Connection

```powershell
adb devices
# Should show your phone as "device"
```

### Step 3: Find Your PC IP

```powershell
ipconfig
# Look for "IPv4 Address" (e.g., 192.168.1.100)
```

### Step 4: Update LoginActivity.kt

Change the URL from 10.0.2.2 to your PC IP:

```kotlin
// Find this line in LoginActivity.kt:
.url("http://10.0.2.2/scheduling-api/login.php")

// Change to your PC IP (example):
.url("http://192.168.1.100/scheduling-api/login.php")
```

Also update testConnectivity():
```kotlin
.url("http://192.168.1.100/scheduling-api/test.php")
```

### Step 5: Ensure Same Network

Make sure your phone is on the **same WiFi network** as your PC.

### Step 6: Test

Run the app on your phone. Should connect to backend.

## Quick Comparison

| Method | Pros | Cons |
|--------|------|------|
| **Recreate Emulator** | Works with 10.0.2.2, no code changes | Takes 5 minutes |
| **Physical Device** | Real device testing, more reliable | Need USB cable, need to change IP in code |

## Step-by-Step: Recreate Emulator

```
1. Android Studio → Device Manager
2. Right-click emulator → Delete
3. Device Manager → Create Virtual Device
4. Select Pixel 4 → API 33
5. Click Next → Show Advanced Settings
6. Verify Network is enabled
7. Click Finish
8. Run your app
9. Test: adb shell ping -c 4 10.0.2.2
```

**Time: ~5 minutes**

## Step-by-Step: Use Physical Device

```
1. Connect phone via USB
2. Enable USB Debugging on phone
3. Get PC IP: ipconfig
4. Update LoginActivity.kt with PC IP
5. Run app on phone
6. Test login
```

**Time: ~3 minutes**

## Recommended: Use Physical Device

Since you already have the backend working (curl succeeded), using a physical device is faster:

1. Connect phone
2. Update one line in LoginActivity.kt
3. Run app
4. Done!

## Testing After Fix

### If Using Recreated Emulator

```powershell
adb shell ping -c 4 10.0.2.2
# Should show 4 successful pings
```

### If Using Physical Device

```powershell
adb devices
# Should show your phone
```

Then run the app and test login.

## Network Diagram - Recreated Emulator

```
┌─────────────────────────────────────┐
│    Your Windows PC                  │
│  ┌───────────────────────────────┐  │
│  │  XAMPP (Apache)               │  │
│  │  http://localhost:80          │  │
│  └───────────────────────────────┘  │
│           ↑                          │
│           │ (10.0.2.2)              │
│           │ (NOW WORKING)           │
│  ┌───────────────────────────────┐  │
│  │  Android Emulator (NEW)       │  │
│  │  Network: Enabled             │  │
│  │  Can reach host               │  │
│  └───────────────────────────────┘  │
└─────────────────────────────────────┘
```

## Network Diagram - Physical Device

```
┌─────────────────────────────────────┐
│    Your Windows PC                  │
│    IP: 192.168.1.100                │
│  ┌───────────────────────────────┐  │
│  │  XAMPP (Apache)               │  │
│  │  http://192.168.1.100:80      │  │
│  └───────────────────────────────┘  │
│           ↑                          │
│           │ (WiFi)                  │
│           │ (Same network)          │
│  ┌───────────────────────────────┐  │
│  │  Android Phone                │  │
│  │  Connected via WiFi           │  │
│  │  Can reach PC                 │  │
│  └───────────────────────────────┘  │
└─────────────────────────────────────┘
```

## Important Notes

- Emulator network isolation is a known issue
- Recreating emulator usually fixes it
- Physical device is more reliable for testing
- 10.0.2.2 only works with properly configured emulator
- Physical device requires code change but is faster to set up

## Next Steps

**Choose one:**

**Option A: Recreate Emulator (5 minutes)**
1. Delete current emulator
2. Create new one with network enabled
3. Test ping to 10.0.2.2
4. Run app

**Option B: Use Physical Device (3 minutes)**
1. Connect phone via USB
2. Get PC IP
3. Update LoginActivity.kt
4. Run app on phone

I recommend **Option B** (physical device) since it's faster and more reliable!
