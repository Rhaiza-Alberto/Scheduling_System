# Immediate Action Plan - Fix Network Unreachable

## Your Current Status

```
✅ adb working
✅ Emulator running (emulator-5554)
❌ Network unreachable (10.0.2.2)
```

Error: `connect: Network is unreachable`

## What to Do Now (In Order)

### Step 1: Restart Emulator (30 seconds)

In PowerShell:
```powershell
adb emu kill
```

Wait 5 seconds, then in **Android Studio**:
1. Click the green **Run** button
2. Select your emulator
3. App starts and emulator restarts

### Step 2: Test Again (30 seconds)

In PowerShell:
```powershell
adb shell ping -c 4 10.0.2.2
```

**If you see 4 successful pings:**
- ✅ Network is fixed!
- Go to Step 5 (Run App)

**If you still see "Network is unreachable":**
- Continue to Step 3

### Step 3: Wipe Emulator Data (2 minutes)

In **Android Studio**:
1. **Tools** → **Device Manager**
2. Right-click your emulator
3. Click **Wipe Data**
4. Click **Run** to restart

Then test:
```powershell
adb shell ping -c 4 10.0.2.2
```

**If it works now:**
- ✅ Go to Step 5 (Run App)

**If still not working:**
- Continue to Step 4

### Step 4: Check Windows Firewall (2 minutes)

Run this PowerShell **as Administrator**:
```powershell
New-NetFirewallRule -DisplayName "Allow HTTP Port 80" -Direction Inbound -Action Allow -Protocol TCP -LocalPort 80
```

Then restart Apache in XAMPP.

Test again:
```powershell
adb shell ping -c 4 10.0.2.2
```

### Step 5: Run Your App

Once ping works:

1. In **Android Studio**, click **Run**
2. Select your emulator
3. App starts
4. Watch **Logcat** (filter: `LoginActivity`)
5. Should see: `✓ Test Connection - Code: 200`

### Step 6: Test Login

1. Email: `admin@example.com`
2. Password: `password123`
3. Click **Sign In**
4. Should see: `Welcome, John Michael Doe!`

## Quick Reference

| Step | Command | Expected Result |
|------|---------|-----------------|
| 1 | `adb emu kill` | Emulator restarts |
| 2 | `adb shell ping -c 4 10.0.2.2` | 4 successful pings |
| 3 | Wipe data in Android Studio | Emulator resets |
| 4 | Add firewall rule | Port 80 allowed |
| 5 | Run app | Logcat shows connection |
| 6 | Login | Welcome message |

## Automated Script

I created a script to help:

```powershell
cd "C:\Users\alely\OneDrive\Projects\MAD\Scheduling_System"
.\fix_emulator_network.ps1
```

This script provides a menu to:
1. Restart emulator
2. Check network status
3. Test connectivity
4. Add firewall exception

## Most Likely Fix

**90% of the time, restarting the emulator fixes this issue:**

```powershell
adb emu kill
# Then run app from Android Studio
```

Try this first!

## If Still Not Working

Run diagnostic commands:

```powershell
# Check if emulator can reach localhost
adb shell ping -c 4 127.0.0.1

# Check if emulator can reach internet
adb shell ping -c 4 8.8.8.8

# Check emulator network config
adb shell ifconfig
```

If these fail, the emulator network is completely broken. In that case:
- Wipe emulator data
- Or use a physical device instead

## Physical Device Alternative

If emulator doesn't work after all fixes:

1. Connect Android phone via USB
2. Find your PC IP: `ipconfig` → IPv4 Address
3. Update LoginActivity.kt:
   ```kotlin
   .url("http://192.168.1.100/scheduling-api/login.php")
   // Replace 192.168.1.100 with your actual IP
   ```
4. Test on phone

## Timeline

- **Step 1-2**: 1 minute (usually fixes it)
- **Step 3**: 2 minutes (if needed)
- **Step 4**: 2 minutes (if needed)
- **Step 5-6**: 2 minutes (if network fixed)

**Total: 3-7 minutes to get working**

## Success Indicators

✅ You'll know it's working when:
- `adb shell ping -c 4 10.0.2.2` shows 4 successful pings
- Logcat shows: `✓ Test Connection - Code: 200`
- Login works with test credentials
- You see: `Welcome, John Michael Doe!`

## Next Steps

1. **Do Step 1 now**: `adb emu kill`
2. **Run app from Android Studio**
3. **Do Step 2**: Test ping again
4. **Let me know the result!**

The most likely fix is just restarting the emulator. Try that first!
