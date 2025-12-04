# Database Schema Fix

## Problem Identified

Your curl command was correct, but login.php was failing because:

1. **Table name case mismatch**: Your schema uses capitalized table names (`Person`, `Account_Type`, `Name`) but login.php was using lowercase (`person`, `account_type`, `name`)
2. **Column name mismatch**: Your `Name` table has `name_second` column but login.php was only looking for `name_middle`

## Root Cause

MySQL table and column names are **case-sensitive on Linux/Mac** but **case-insensitive on Windows**. However, it's best practice to match exactly.

## Solution Applied

### Fixed login.php

Changed the SQL query from:
```sql
FROM person p
JOIN account_type a ON p.account_ID = a.account_ID
LEFT JOIN name n ON p.name_ID = n.name_ID
```

To:
```sql
FROM Person p
JOIN Account_Type a ON p.account_ID = a.account_ID
LEFT JOIN Name n ON p.name_ID = n.name_ID
```

Also updated column selection to include `name_second`:
```sql
SELECT p.person_ID, p.person_username, p.person_password, 
       p.account_ID, a.account_name, 
       n.name_first, n.name_second, n.name_middle, n.name_last, n.name_suffix
```

## Your Database Schema

```
Account_Type (account_ID, account_name)
Day (day_ID, day_name)
Name (name_ID, name_first, name_second, name_middle, name_last, name_suffix)
Person (person_ID, person_username, person_password, account_ID, name_ID)
Teacher (teacher_ID, person_ID)
Room (room_ID, room_name, room_capacity)
Section (section_ID, section_name, section_year)
Subject (subject_ID, subject_code, subject_name)
Time (time_ID, time_start, time_end)
Schedule (schedule_ID, day_ID, subject_ID, section_ID, teacher_ID, time_ID, room_ID, schedule_status)
```

## Setup Instructions

### Step 1: Insert Test Data
Run this in your browser or PowerShell:
```
http://localhost/scheduling-api/insert_test_data.php
```

Or use PowerShell:
```powershell
cd C:\Users\alely\OneDrive\Projects\MAD\Scheduling_System\backend
.\test_login_fixed.ps1
```

This will insert:
- **Account Types**: Admin, Teacher, Student
- **Names**: John Doe, Jane Smith, Michael Johnson
- **Persons**: 3 test users with credentials
- **Days**: Monday through Sunday
- **Time Slots**: 8 time slots from 08:00 to 17:00

### Step 2: Test Login
```bash
curl -X POST http://localhost/scheduling-api/login.php \
  -H "Content-Type: application/json" \
  -d '{"username":"admin@example.com","password":"password123"}'
```

### Step 3: Run Android App
The app should now successfully connect and login.

## Test Credentials

| Email | Password | Role |
|-------|----------|------|
| admin@example.com | password123 | Admin |
| teacher@example.com | password123 | Teacher |
| student@example.com | password123 | Student |

## Files Modified

- **login.php** - Fixed table and column names to match your schema

## Files Created

- **insert_test_data.php** - Inserts test data into your database
- **test_login_fixed.ps1** - PowerShell script to test the fixed login
- **DATABASE_SCHEMA_FIX.md** - This file

## Verification

After inserting test data, verify with:
```bash
# Check if admin user exists
curl http://localhost/scheduling-api/verify_setup.php
```

Should show:
```json
{
  "checks": {
    "database": {
      "connection": "OK",
      "table_person": "OK",
      "test_user_admin": "OK"
    }
  }
}
```

## Next Steps

1. ✅ Run `insert_test_data.php` to populate database
2. ✅ Test with `test_login_fixed.ps1`
3. ✅ Run Android app and test login
4. ✅ Check Logcat for successful connection

The curl command you used was correct - the issue was the database schema mismatch!
