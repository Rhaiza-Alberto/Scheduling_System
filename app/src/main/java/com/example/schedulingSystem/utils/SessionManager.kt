package com.example.schedulingSystem.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.schedulingSystem.network.User
import com.google.gson.Gson

/**
 * SessionManager - Handles user authentication state and session persistence
 * Uses SharedPreferences to store user data locally
 */
class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREF_NAME,
        Context.MODE_PRIVATE
    )

    private val editor: SharedPreferences.Editor = prefs.edit()
    private val gson = Gson()

    companion object {
        private const val PREF_NAME = "scheduling_system_session"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_DATA = "user_data"
        private const val KEY_PERSON_ID = "person_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_ACCOUNT_TYPE = "account_type"
        private const val KEY_ACCOUNT_ID = "account_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_LOGIN_TIME = "login_time"
    }

    /**
     * Save user session after successful login
     * @param user User object from login response
     */
    fun saveUserSession(user: User) {
        editor.apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USER_DATA, gson.toJson(user))
            putInt(KEY_PERSON_ID, user.person_ID)
            putString(KEY_USERNAME, user.username)
            putString(KEY_ACCOUNT_TYPE, user.account_type)
            putInt(KEY_ACCOUNT_ID, user.account_ID)
            putString(KEY_USER_NAME, user.name)
            putLong(KEY_LOGIN_TIME, System.currentTimeMillis())
            apply()
        }
    }

    /**
     * Get complete user data object
     * @return User object or null if not logged in
     */
    fun getUserData(): User? {
        val userJson = prefs.getString(KEY_USER_DATA, null) ?: return null
        return try {
            gson.fromJson(userJson, User::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Check if user is currently logged in
     * @return true if logged in, false otherwise
     */
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false) && getUserData() != null
    }

    /**
     * Check if current user is an admin
     * @return true if admin, false otherwise
     */
    fun isAdmin(): Boolean {
        return getUserData()?.account_type?.equals("Admin", ignoreCase = true) == true
    }

    /**
     * Check if current user is a teacher/professor
     * @return true if teacher, false otherwise
     */
    fun isTeacher(): Boolean {
        return getUserData()?.account_type?.equals("Teacher", ignoreCase = true) == true
    }

    /**
     * Get the current user's person ID
     * @return person ID or -1 if not logged in
     */
    fun getPersonId(): Int {
        return prefs.getInt(KEY_PERSON_ID, -1)
    }

    /**
     * Get the current user's username
     * @return username or null if not logged in
     */
    fun getUsername(): String? {
        return prefs.getString(KEY_USERNAME, null)
    }

    /**
     * Get the current user's display name
     * @return user's full name or null if not logged in
     */
    fun getUserName(): String? {
        return prefs.getString(KEY_USER_NAME, null)
    }

    /**
     * Get the current user's account type
     * @return account type (Admin/Teacher) or null if not logged in
     */
    fun getAccountType(): String? {
        return prefs.getString(KEY_ACCOUNT_TYPE, null)
    }

    /**
     * Get the current user's account ID
     * @return account ID or -1 if not logged in
     */
    fun getAccountId(): Int {
        return prefs.getInt(KEY_ACCOUNT_ID, -1)
    }

    /**
     * Get the login timestamp
     * @return timestamp in milliseconds or 0 if never logged in
     */
    fun getLoginTime(): Long {
        return prefs.getLong(KEY_LOGIN_TIME, 0)
    }

    /**
     * Check if session is still valid (within 24 hours)
     * @return true if session is valid, false otherwise
     */
    fun isSessionValid(): Boolean {
        if (!isLoggedIn()) return false

        val loginTime = getLoginTime()
        val currentTime = System.currentTimeMillis()
        val sessionDuration = currentTime - loginTime
        val twentyFourHours = 24 * 60 * 60 * 1000L // 24 hours in milliseconds

        return sessionDuration < twentyFourHours
    }

    /**
     * Update user data (useful when profile is edited)
     * @param user Updated user object
     */
    fun updateUserData(user: User) {
        saveUserSession(user)
    }

    /**
     * Clear all session data (logout)
     * Removes all stored user information
     */
    fun clearSession() {
        editor.apply {
            clear()
            apply()
        }
    }

    /**
     * Check if specific permission/feature is allowed for current user
     * @param feature Feature name to check
     * @return true if allowed, false otherwise
     */
    fun hasPermission(feature: String): Boolean {
        return when (feature) {
            "manage_rooms" -> isAdmin()
            "approve_requests" -> isAdmin()
            "create_request" -> isTeacher()
            "view_schedule" -> isLoggedIn()
            else -> false
        }
    }

    /**
     * Get a summary of current session for debugging
     * @return String containing session information
     */
    fun getSessionSummary(): String {
        return if (isLoggedIn()) {
            """
            Session Active
            User: ${getUserName()}
            Username: ${getUsername()}
            Account Type: ${getAccountType()}
            Person ID: ${getPersonId()}
            Login Time: ${android.text.format.DateFormat.format("yyyy-MM-dd HH:mm:ss", getLoginTime())}
            """.trimIndent()
        } else {
            "No active session"
        }
    }

    /**
     * Check if this is the first launch after installation
     * @return true if first launch, false otherwise
     */
    fun isFirstLaunch(): Boolean {
        val isFirst = prefs.getBoolean("is_first_launch", true)
        if (isFirst) {
            editor.putBoolean("is_first_launch", false).apply()
        }
        return isFirst
    }

    /**
     * Save a preference value
     * @param key Preference key
     * @param value Value to save
     */
    fun savePreference(key: String, value: String) {
        editor.putString(key, value).apply()
    }

    /**
     * Get a preference value
     * @param key Preference key
     * @param defaultValue Default value if key doesn't exist
     * @return Stored value or default
     */
    fun getPreference(key: String, defaultValue: String = ""): String {
        return prefs.getString(key, defaultValue) ?: defaultValue
    }
}