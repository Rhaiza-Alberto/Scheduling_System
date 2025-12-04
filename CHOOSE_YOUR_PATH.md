# Choose Your Path - Fix Emulator Network Issue

## Your Problem

Emulator is isolated - cannot reach external networks (8.8.8.8 or 10.0.2.2).

## Two Solutions

### Path A: Recreate Emulator ⏱️ 5 minutes

**Pros:**
- No code changes needed
- Uses 10.0.2.2 (standard approach)
- Works with existing LoginActivity

**Cons:**
- Takes 5 minutes
- Need to wait for emulator to start

**Steps:**
1. Delete current emulator
2. Create new one with network enabled
3. Run app
4. Test

### Path B: Use Physical Device ⏱️ 3 minutes

**Pros:**
- Faster setup (3 minutes)
- More reliable
- Real device testing
- Better for debugging

**Cons:**
- Need to change one line in code
- Need USB cable
- Phone must be on same WiFi

**Steps:**
1. Connect phone via USB
2. Get PC IP
3. Update LoginActivity.kt (1 line)
4. Run app on phone

---

## Quick Decision Matrix

| Question | Path A | Path B |
|----------|--------|--------|
| Have USB cable? | ✓ | ✓ Required |
| Want to change code? | No | Yes (1 line) |
| Have Android phone? | ✓ | ✓ Required |
| Time available? | 5 min | 3 min |
| Prefer emulator? | Yes | No |

---

## My Recommendation: **Path B (Physical Device)**

**Why?**
- Faster (3 min vs 5 min)
- More reliable (no network isolation)
- Real device testing
- Only 1 line of code change

---

## Path A: Recreate Emulator (Detailed)

### Step 1: Delete Emulator (1 minute)

In Android Studio:
1. **Tools** → **Device Manager**
2. Right-click `emulator-5554`
3. Click **Delete**
4. Confirm

### Step 2: Create New Emulator (3 minutes)

1. **Device Manager** → **Create Virtual Device**
2. Select: **Pixel 4**
3. Select: **API 33**
4. Click **Next**
5. Click **Show Advanced Settings**
6. Verify **Network** is enabled (not disabled)
7. Click **Finish**

### Step 3: Test (1 minute)

Run your app. Then:
```powershell
adb shell ping -c 4 10.0.2.2
# Should show 4 successful pings
```

If it works, you're done! Run the app and test login.

---

## Path B: Use Physical Device (Detailed)

### Step 1: Connect Phone (1 minute)

1. Connect Android phone via USB cable
2. On phone: Settings → Developer Options → USB Debugging (enable)
3. Trust the computer when prompted

### Step 2: Get PC IP (30 seconds)

In PowerShell:
```powershell
ipconfig
```

Look for line like:
```
IPv4 Address. . . . . . . . . . . : 192.168.1.100
```

Copy this IP (e.g., `192.168.1.100`)

### Step 3: Update Code (30 seconds)

In LoginActivity.kt, find these lines:

```kotlin
// Line ~55 (testConnectivity)
.url("http://10.0.2.2/scheduling-api/test.php")

// Line ~110 (performLogin)
.url("http://10.0.2.2/scheduling-api/login.php")
```

Change both to your PC IP:
```kotlin
// Replace 192.168.1.100 with YOUR IP from ipconfig
.url("http://192.168.1.100/scheduling-api/test.php")
.url("http://192.168.1.100/scheduling-api/login.php")
```

### Step 4: Run App (1 minute)

1. In Android Studio, click **Run**
2. Select your phone (not emulator)
3. App installs and runs on phone
4. Test login with: `admin@example.com` / `password123`

---

## Code Changes for Path B

**File:** `LoginActivity.kt`

**Change 1 (Line ~55):**
```kotlin
// BEFORE:
.url("http://10.0.2.2/scheduling-api/test.php")

// AFTER:
.url("http://192.168.1.100/scheduling-api/test.php")
```

**Change 2 (Line ~110):**
```kotlin
// BEFORE:
.url("http://10.0.2.2/scheduling-api/login.php")

// AFTER:
.url("http://192.168.1.100/scheduling-api/login.php")
```

Replace `192.168.1.100` with your actual PC IP from `ipconfig`.

---

## Verify Phone is on Same Network

**On your phone:**
1. Settings → WiFi
2. Connect to your WiFi network
3. Note the network name

**On your PC:**
1. Check you're on the same WiFi network
2. Or use Ethernet (if PC is wired)

---

## Test After Setup

### Path A (Emulator):
```powershell
adb shell ping -c 4 10.0.2.2
# Should work
```

### Path B (Phone):
```powershell
adb devices
# Should show your phone
```

Then run app and test login.

---

## Success Indicators

✅ You'll know it worked when:
- App shows: `✓ Backend connected!` toast
- Logcat shows: `✓ Test Connection - Code: 200`
- Login works with test credentials
- You see: `Welcome, John Michael Doe!`

---

## Which Should I Choose?

**Choose Path A if:**
- You prefer emulator testing
- You have 5 minutes to spare
- You don't have a phone handy

**Choose Path B if:**
- You want faster setup (3 min)
- You have an Android phone
- You want more reliable testing

---

## I Recommend: Path B

**Fastest and most reliable!**

1. Connect phone (1 min)
2. Get PC IP (30 sec)
3. Update 2 lines in code (30 sec)
4. Run app (1 min)
5. Test login (30 sec)

**Total: 3 minutes**

---

## Next Steps

**Pick one path above and let me know:**
- Path A: I'll help you recreate the emulator
- Path B: I'll help you update the code for physical device

Which would you prefer?
