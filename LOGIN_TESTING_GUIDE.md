# LoginActivity Connection Testing Guide

## Fixed Issues

✅ **Removed `:80` port** from login URL (was causing issues)
✅ **Created test.php** for connectivity verification
✅ **Added timeouts** to prevent hanging requests
✅ **Improved error logging** with clear indicators (→ ← ✓ ✗)
✅ **Added empty response handling** to catch server errors

## Prerequisites

### 1. XAMPP Setup
- Ensure XAMPP is running (Apache + MySQL)
- Apache should be on port 80
- MySQL should be on port 3306

### 2. Database Setup
```bash
# In phpMyAdmin or MySQL command line:
1. Create database: scheduling-system
2. Import backend/sample_data.sql
```

### 3. Backend Files Location
```
C:\xampp\htdocs\scheduling-api\
├── config.php
├── login.php
├── test.php (newly created)
├── register.php
├── get_account_types.php
├── verify_token.php
└── index.php
```

## Testing Steps

### Step 1: Verify XAMPP is Running
- Open browser: `http://localhost/`
- Should see XAMPP dashboard

### Step 2: Test Backend Connectivity
- Open browser: `http://localhost/scheduling-api/test.php`
- Should see: `{"success":true,"message":"Connection successful","timestamp":"..."}`

### Step 3: Test Login Endpoint (Postman or curl)
```bash
# Using curl:
curl -X POST http://localhost/scheduling-api/login.php \
  -H "Content-Type: application/json" \
  -d '{"username":"admin@example.com","password":"password123"}'

# Expected response:
{
  "success": true,
  "message": "Login successful",
  "user": {
    "person_ID": 1,
    "username": "admin@example.com",
    "account_type": "Admin",
    "account_ID": 1,
    "name": "John Doe"
  }
}
```

### Step 4: Run Android App
1. Open Android Studio
2. Run the app on emulator
3. Watch Logcat for messages:
   - `✓ Test Connection - Code: 200` → Backend is reachable
   - `✗ Test Connection Failed` → Check XAMPP and network

### Step 5: Test Login
1. Enter credentials:
   - Email: `admin@example.com`
   - Password: `password123`
2. Click Sign In
3. Check Logcat:
   - `→ Sending login request for: admin@example.com`
   - `← Response Code: 200`
   - `← Response Body: {...}`

## Test Credentials

| Role | Email | Password |
|------|-------|----------|
| Admin | admin@example.com | password123 |
| Teacher | teacher@example.com | password123 |
| Student | student@example.com | password123 |

## Troubleshooting

### "Backend unreachable"
- [ ] XAMPP Apache is running
- [ ] Check firewall settings
- [ ] Verify `http://localhost/scheduling-api/test.php` works in browser
- [ ] Check Android emulator network settings

### "Empty response from server"
- [ ] Check PHP error logs: `C:\xampp\apache\logs\error.log`
- [ ] Verify config.php database credentials
- [ ] Ensure database exists and has data

### "Invalid username or password"
- [ ] Verify sample_data.sql was imported
- [ ] Check database has person table with test data
- [ ] Verify password is exactly: `password123`

### "Database connection failed"
- [ ] MySQL is running
- [ ] Database name is `scheduling-system`
- [ ] User is `root` with no password
- [ ] Check config.php settings

## Logcat Filtering

In Android Studio, filter logs:
```
LoginActivity
```

This will show all connection attempts and responses.

## Next Steps After Successful Login

1. **Admin Dashboard** - Should show admin management options
2. **Teacher Dashboard** - Should show teacher schedule view
3. **Session Persistence** - Data saved in SharedPreferences
4. **Logout** - Clears session and returns to login

## Files Modified

- `LoginActivity.kt` - Fixed URL, added timeouts, improved logging
- `test.php` - Created for connectivity testing
- `LOGIN_TESTING_GUIDE.md` - This file

## Quick Debug Checklist

```
[ ] XAMPP running (Apache + MySQL)
[ ] Database "scheduling-system" exists
[ ] sample_data.sql imported
[ ] http://localhost/scheduling-api/test.php returns JSON
[ ] Emulator can reach 10.0.2.2 (host machine)
[ ] LoginActivity shows "Backend connected!" toast
[ ] Login attempt shows response in Logcat
```
