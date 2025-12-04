# Setup ADB Path - Fix "adb is not recognized"

## Problem
```
adb : The term 'adb' is not recognized as the name of a cmdlet, function, script file, or operable program.
```

This means `adb` (Android Debug Bridge) is not in your system PATH.

## Solution 1: Use Full Path to ADB (Quick Fix)

Instead of just `adb`, use the full path:

```powershell
"C:\Users\alely\AppData\Local\Android\Sdk\platform-tools\adb.exe" shell ping -c 4 10.0.2.2
```

This should work immediately.

## Solution 2: Add ADB to System PATH (Permanent Fix)

### Step 1: Find Your Android SDK Path

Usually it's one of these:
- `C:\Users\alely\AppData\Local\Android\Sdk`
- `C:\Android\sdk`
- Check in Android Studio: File → Settings → Appearance & Behavior → System Settings → Android SDK

The `adb` tool is in: `<SDK_PATH>\platform-tools\`

### Step 2: Add to Windows PATH

**Option A: Using GUI (Easiest)**

1. Press `Win + X` → **System**
2. Click **Advanced system settings**
3. Click **Environment Variables** button
4. Under "User variables" or "System variables", click **New**
5. **Variable name**: `PATH`
6. **Variable value**: `C:\Users\alely\AppData\Local\Android\Sdk\platform-tools`
7. Click **OK** three times
8. **Restart PowerShell** (close and reopen)

**Option B: Using PowerShell (Advanced)**

```powershell
# Run as Administrator
$sdkPath = "C:\Users\alely\AppData\Local\Android\Sdk\platform-tools"
$currentPath = [Environment]::GetEnvironmentVariable("PATH", "User")
$newPath = "$currentPath;$sdkPath"
[Environment]::SetEnvironmentVariable("PATH", $newPath, "User")
```

Then restart PowerShell.

### Step 3: Verify It Works

Close and reopen PowerShell, then run:
```powershell
adb --version
```

Should show version info.

## Quick Test Commands

After fixing PATH, run these in PowerShell:

```powershell
# Check adb is working
adb --version

# List connected devices
adb devices

# Ping from emulator
adb shell ping -c 4 10.0.2.2

# Test HTTP from emulator
adb shell curl http://10.0.2.2/scheduling-api/test.php
```

## If You Don't Know Your SDK Path

Run this in PowerShell:

```powershell
# Find Android SDK
Get-ChildItem -Path "C:\Users\alely\AppData\Local\Android" -Recurse -Filter "adb.exe" | Select-Object FullName
```

This will show the full path to adb.exe.

## Alternative: Use Android Studio Terminal

Android Studio has a built-in terminal that already has adb in PATH:

1. **Android Studio** → **View** → **Tool Windows** → **Terminal**
2. Or press `Alt + F12`
3. Run commands directly:
   ```bash
   adb shell ping -c 4 10.0.2.2
   ```

This terminal already has adb configured!

## Recommended: Use Android Studio Terminal

The easiest solution is to use **Android Studio's built-in Terminal**:

1. Open Android Studio
2. **View** → **Tool Windows** → **Terminal** (or `Alt + F12`)
3. Run your adb commands there
4. No PATH setup needed!

## Verification

After setup, you should be able to run:

```powershell
adb shell ping -c 4 10.0.2.2
```

And see:
```
PING 10.0.2.2 (10.0.2.2) 56(84) bytes of data.
64 bytes from 10.0.2.2: icmp_seq=1 ttl=63 time=0.123 ms
64 bytes from 10.0.2.2: icmp_seq=2 ttl=63 time=0.156 ms
64 bytes from 10.0.2.2: icmp_seq=3 ttl=63 time=0.145 ms
64 bytes from 10.0.2.2: icmp_seq=4 ttl=63 time=0.134 ms
```

## Quick Reference

| What | Command |
|------|---------|
| Check adb version | `adb --version` |
| List devices | `adb devices` |
| Ping from emulator | `adb shell ping -c 4 10.0.2.2` |
| Test HTTP | `adb shell curl http://10.0.2.2/scheduling-api/test.php` |
| Restart emulator | `adb emu kill` |

## Troubleshooting

**Still getting "adb is not recognized"?**

1. Verify SDK path exists: `C:\Users\alely\AppData\Local\Android\Sdk\platform-tools\adb.exe`
2. Use full path: `"C:\Users\alely\AppData\Local\Android\Sdk\platform-tools\adb.exe" shell ping -c 4 10.0.2.2`
3. Or use Android Studio Terminal (easiest)

**PATH not working after adding?**

1. Close and reopen PowerShell completely
2. Or restart your computer
3. Or use Android Studio Terminal instead
