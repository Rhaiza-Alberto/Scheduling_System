package com.example.schedulingSystem

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.schedulingSystem.adapters.AdminUserAdapter
import com.example.schedulingSystem.models.UserItem
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class AdminManageUsersActivity : AppCompatActivity() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val BACKEND_URL = "http://10.0.2.2/scheduling-api"
    }

    private lateinit var userAdapter: AdminUserAdapter
    private lateinit var rvUsers: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // === Security Check ===
        val prefs = getSharedPreferences("user_session", MODE_PRIVATE)
        val isLoggedIn = prefs.getBoolean("is_logged_in", false)
        val accountType = prefs.getString("account_type", "")

        if (!isLoggedIn || accountType?.lowercase() != "admin") {
            Log.w("AdminManageUsers", "Access denied: Not admin")
            Toast.makeText(this, "Access denied: Admin only", Toast.LENGTH_LONG).show()
            redirectToLogin()
            return
        }

        setContentView(R.layout.activity_admin_dashboard) // Reuse same layout (tabs + structure)

        // Handle back press with modern API
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Go back to dashboard instead of closing app
                startActivity(Intent(this@AdminManageUsersActivity, AdminDashboardActivity::class.java))
                finish()
            }
        })

        // Initialize RecyclerView
        rvUsers = findViewById(R.id.containerRooms) // Reusing same ID — will show users now
        userAdapter = AdminUserAdapter()
        rvUsers.apply {
            layoutManager = LinearLayoutManager(this@AdminManageUsersActivity)
            adapter = userAdapter
        }

        setupClickListeners()
        updateGreetingAndTitle()
        highlightUsersTab()
        loadUsersFromApi()
    }

    private fun setupClickListeners() {
        // Settings → Logout
        findViewById<ImageButton>(R.id.btnSettings).setOnClickListener { performLogout() }

        // Tab Navigation
        val tabSchedules = findViewById<TextView>(R.id.tabSchedules)
        val tabUsers = findViewById<TextView>(R.id.tabUsers)
        val tabRooms = findViewById<TextView>(R.id.tabRooms)

        tabSchedules.setOnClickListener {
            updateTabSelection(tabSchedules, tabUsers, tabRooms)
            startActivity(Intent(this, AdminDashboardActivity::class.java))
            finish() // Go back to dashboard
        }

        tabUsers.setOnClickListener {
            // Already here
            updateTabSelection(tabUsers, tabSchedules, tabRooms)
            Toast.makeText(this, "Manage Users", Toast.LENGTH_SHORT).show()
        }

        tabRooms.setOnClickListener {
            updateTabSelection(tabRooms, tabSchedules, tabUsers)
            startActivity(Intent(this, AdminManageRoomsActivity::class.java))
            finish()
        }

        // Hide or disable FAB menu if not needed here (optional)
        // findViewById<ImageButton>(R.id.btnMain).visibility = View.GONE
    }

    private fun updateGreetingAndTitle() {
        findViewById<TextView>(R.id.tvGreeting).text = "Manage Users"
        findViewById<TextView>(R.id.tvManageTitle).text = "All Users"
    }

    private fun highlightUsersTab() {
        val tabSchedules = findViewById<TextView>(R.id.tabSchedules)
        val tabUsers = findViewById<TextView>(R.id.tabUsers)
        val tabRooms = findViewById<TextView>(R.id.tabRooms)

        updateTabSelection(tabUsers, tabSchedules, tabRooms)
    }

    // Reuse same tab selection logic
    private fun updateTabSelection(selected: TextView, vararg others: TextView) {
        selected.apply {
            setBackgroundResource(R.drawable.bg_input_outline)
            backgroundTintList = ContextCompat.getColorStateList(this@AdminManageUsersActivity, R.color.white)
            setTextColor(ContextCompat.getColor(this@AdminManageUsersActivity, R.color.primary_dark_green))
            setTypeface(null, android.graphics.Typeface.BOLD)
            elevation = 4f
        }

        others.forEach { tab ->
            tab.apply {
                background = null
                backgroundTintList = null
                setTextColor(ContextCompat.getColor(this@AdminManageUsersActivity, R.color.white))
                setTypeface(null, android.graphics.Typeface.NORMAL)
                elevation = 0f
            }
        }
    }

    private fun loadUsersFromApi() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("AdminManageUsers", "Fetching users from API...")
                val response = client.newCall(
                    Request.Builder()
                        .url("$BACKEND_URL/get_users.php") // Make sure this endpoint exists!
                        .build()
                ).execute()

                val jsonData = response.body?.string() ?: ""
                Log.d("AdminManageUsers", "Response: $jsonData")

                val json = JSONObject(jsonData)
                if (json.getBoolean("success")) {
                    val usersArray = json.getJSONArray("users")
                    val userList = mutableListOf<UserItem>()

                    for (i in 0 until usersArray.length()) {
                        val obj = usersArray.getJSONObject(i)

                        userList.add(
                            UserItem(
                                userId = obj.getInt("id"),
                                fullName = obj.getString("full_name"),
                                email = obj.getString("email"),
                                accountType = obj.getString("account_type"),
                                status = if (obj.optInt("is_approved", 1) == 1) "Approved" else "Pending"
                            )
                        )
                    }

                    withContext(Dispatchers.Main) {
                        userAdapter.submitList(userList)
                        Toast.makeText(
                            this@AdminManageUsersActivity,
                            "Loaded ${userList.size} users",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@AdminManageUsersActivity,
                            "Error: ${json.optString("message", "Failed to load users")}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("AdminManageUsers", "Error loading users", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@AdminManageUsersActivity,
                        "Network error: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun performLogout() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                getSharedPreferences("user_session", MODE_PRIVATE).edit { clear() }
                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
                redirectToLogin()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun redirectToLogin() {
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}