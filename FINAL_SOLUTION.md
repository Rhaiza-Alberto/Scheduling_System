# Final Solution - Use Physical Device or Update Emulator URL

## Your Situation

✅ Backend is working (curl succeeded)
✅ Emulator can reach localhost (127.0.0.1)
❌ Emulator cannot reach host (10.0.2.2)

**Solution:** Use your actual PC IP instead of 10.0.2.2

## Step 1: Find Your PC IP

In PowerShell:
```powershell
ipconfig
```

Look for a line like:
```
IPv4 Address. . . . . . . . . . . : 192.168.1.100
```

Copy this IP address (e.g., `192.168.1.100`)

## Step 2: Update LoginActivity.kt

Replace `10.0.2.2` with your PC IP in two places:

### Location 1: testConnectivity() method (Line ~60)

**Find:**
```kotlin
.url("http://10.0.2.2/scheduling-api/test.php")
```

**Replace with:**
```kotlin
.url("http://192.168.1.100/scheduling-api/test.php")
```

### Location 2: performLogin() method (Line ~110)

**Find:**
```kotlin
.url("http://10.0.2.2/scheduling-api/login.php")
```

**Replace with:**
```kotlin
.url("http://192.168.1.100/scheduling-api/login.php")
```

**Note:** Replace `192.168.1.100` with YOUR actual IP from ipconfig!

## Step 3: Ensure Network Access

### Option A: Use Physical Device (Recommended)

1. Connect Android phone via USB
2. Enable USB Debugging on phone
3. Phone must be on **same WiFi network** as your PC
4. Run app on phone

### Option B: Use Emulator with Bridged Network

If you want to use emulator:
1. Emulator must be on same network as PC
2. Or use a local network IP that's accessible

## Step 4: Test

1. Run the app
2. Should see: `✓ Backend connected!` toast
3. Try login with: `admin@example.com` / `password123`
4. Should see: `Welcome, John Michael Doe!`

## Important: Network Accessibility

Your PC IP (e.g., 192.168.1.100) must be:
- **Accessible from emulator/phone**
- **On same network** as emulator/phone
- **Not blocked by firewall**

### Check Firewall

Windows Firewall might block the connection:

```powershell
# Run as Administrator
New-NetFirewallRule -DisplayName "Allow XAMPP HTTP" `
    -Direction Inbound `
    -Action Allow `
    -Protocol TCP `
    -LocalPort 80
```

## Example Update

**Before:**
```kotlin
private fun testConnectivity() {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val request = Request.Builder()
                .url("http://10.0.2.2/scheduling-api/test.php")
                .get()
                .build()
```

**After:**
```kotlin
private fun testConnectivity() {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val request = Request.Builder()
                .url("http://192.168.1.100/scheduling-api/test.php")
                .get()
                .build()
```

## Quick Steps

1. Run: `ipconfig` → Copy IPv4 Address
2. Open LoginActivity.kt
3. Replace `10.0.2.2` with your IP (2 places)
4. Run app
5. Test login

## If Using Physical Device

Same steps, but:
1. Connect phone via USB
2. Enable USB Debugging
3. Select phone when running app
4. Phone must be on same WiFi

## Verify XAMPP is Accessible

Test from your PC:
```powershell
curl http://localhost/scheduling-api/test.php
# Should return JSON
```

Test from emulator/phone (after code update):
```powershell
adb shell curl http://192.168.1.100/scheduling-api/test.php
# Should return JSON
```

## Common Issues

| Issue | Solution |
|-------|----------|
| Connection refused | XAMPP not running |
| Network unreachable | Firewall blocking, or wrong IP |
| Wrong IP | Run ipconfig again, use correct IP |
| Emulator can't reach PC | Use physical device instead |

## Recommended: Physical Device

Since emulator network is isolated:
1. Connect Android phone
2. Update code with PC IP
3. Run on phone
4. More reliable

## Files to Update

- `LoginActivity.kt` (2 lines)

That's it! Just update the IP address and it should work.
