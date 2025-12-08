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
import com.example.schedulingSystem.models.User
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class AdminManageUsersActivity : AppCompatActivity() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val BACKEND_URL = "http://10.0.2.2/scheduling-api"
    }

    private lateinit var userAdapter: AdminUserAdapter
    private lateinit var rvUsers: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Security check
        val prefs = getSharedPreferences("user_session", MODE_PRIVATE)
        if (!prefs.getBoolean("is_logged_in", false) ||
            prefs.getString("account_type", "").equals("admin", ignoreCase = true).not()
        ) {
            Toast.makeText(this, "Access denied: Admin only", Toast.LENGTH_LONG).show()
            redirectToLogin()
            return
        }

        setContentView(R.layout.activity_admin_dashboard) // Reuse layout

        // Back press → go to dashboard
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                startActivity(Intent(this@AdminManageUsersActivity, AdminDashboardActivity::class.java))
                finish()
            }
        })

        // Setup RecyclerView
        rvUsers = findViewById(R.id.containerRooms) // Reusing same container
        userAdapter = AdminUserAdapter()
        rvUsers.apply {
            layoutManager = LinearLayoutManager(this@AdminManageUsersActivity)
            adapter = userAdapter
        }

        setupClickListeners()
        updateUIForUsersTab()
        loadUsersFromApi()
    }

    private fun setupClickListeners() {
        findViewById<ImageButton>(R.id.btnSettings).setOnClickListener { performLogout() }

        val tabSchedules = findViewById<TextView>(R.id.tabSchedules)
        val tabUsers = findViewById<TextView>(R.id.tabUsers)
        val tabRooms = findViewById<TextView>(R.id.tabRooms)

        tabSchedules.setOnClickListener {
            startActivity(Intent(this, AdminDashboardActivity::class.java))
            finish()
        }

        tabUsers.setOnClickListener {
            Toast.makeText(this, "You are here: Manage Users", Toast.LENGTH_SHORT).show()
        }

        tabRooms.setOnClickListener {
            startActivity(Intent(this, AdminManageRoomsActivity::class.java))
            finish()
        }

        // Highlight current tab
        updateTabSelection(tabUsers, tabSchedules, tabRooms)
    }

    private fun updateUIForUsersTab() {
        findViewById<TextView>(R.id.tvGreeting)?.text = "Manage Users"
        findViewById<TextView>(R.id.tvManageTitle)?.text = "All System Users"
    }

    private fun updateTabSelection(selected: TextView, vararg others: TextView) {
        selected.apply {
            setBackgroundResource(R.drawable.bg_input_outline)
            backgroundTintList = ContextCompat.getColorStateList(this@AdminManageUsersActivity, R.color.white)
            setTextColor(ContextCompat.getColor(this@AdminManageUsersActivity, R.color.primary_dark_green))
            setTypeface(null, android.graphics.Typeface.BOLD)
            elevation = 4f
        }
        others.forEach {
            it.apply {
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
                Log.d("AdminUsers", "Loading users from API...")
                val request = Request.Builder()
                    .url("$BACKEND_URL/get_all_users.php")
                    .build()

                val response = client.newCall(request).execute()
                val body = response.body?.string() ?: ""

                if (!response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@AdminManageUsersActivity, "Server error: ${response.code}", Toast.LENGTH_LONG).show()
                    }
                    return@launch
                }

                val json = JSONObject(body)
                if (!json.getBoolean("success")) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@AdminManageUsersActivity, json.optString("message", "Failed"), Toast.LENGTH_LONG).show()
                    }
                    return@launch
                }

                val usersArray = json.getJSONArray("users")
                val userList = mutableListOf<User>()

                for (i in 0 until usersArray.length()) {
                    val obj = usersArray.getJSONObject(i)

                    userList.add(
                        User(
                            personId = obj.getInt("person_ID"),
                            email = obj.getString("email"),
                            fullName = obj.getString("full_name"),
                            accountType = obj.getString("account_type"),
                            firstName = obj.optString("first_name", ""),
                            middleName = obj.optString("middle_name", ""),
                            lastName = obj.getString("last_name"),
                            suffix = obj.optString("suffix", "")
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

            } catch (e: Exception) {
                Log.e("AdminUsers", "Load failed", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AdminManageUsersActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun performLogout() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure?")
            .setPositiveButton("Yes") { _, _ ->
                getSharedPreferences("user_session", MODE_PRIVATE).edit { clear() }
                Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
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