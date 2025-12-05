package com.example.schedulingSystem

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import androidx.core.content.edit
import java.util.concurrent.TimeUnit

class AdminDashboardActivity : AppCompatActivity() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val BACKEND_URL = "http://10.0.2.2/scheduling-api"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if user is logged in and is admin
        val prefs = getSharedPreferences("user_session", MODE_PRIVATE)
        val isLoggedIn = prefs.getBoolean("is_logged_in", false)
        val accountType = prefs.getString("account_type", "")

        if (!isLoggedIn) {
            Log.w("AdminDashboard", "User not logged in, redirecting to login")
            redirectToLogin()
            return
        }

        if (accountType?.lowercase() != "admin") {
            Log.w("AdminDashboard", "User is not admin (type: $accountType), redirecting to login")
            Toast.makeText(this, "Access denied: Admin only", Toast.LENGTH_LONG).show()
            redirectToLogin()
            return
        }

        setContentView(R.layout.activity_admin_dashboard)

        setupClickListeners()
        displayUserInfo()
        loadDashboardData()
    }

    private fun setupClickListeners() {
        // Settings button (logout)
        findViewById<ImageButton>(R.id.btnSettings).setOnClickListener {
            performLogout()
        }

        // Review button
        findViewById<MaterialButton>(R.id.btnReview).setOnClickListener {
            Toast.makeText(this, "Review pending approvals - Coming soon", Toast.LENGTH_SHORT).show()
        }

        // Room 1 actions
        findViewById<ImageButton>(R.id.btnEdit1).setOnClickListener {
            Toast.makeText(this, "Edit Lab 101 - Coming soon", Toast.LENGTH_SHORT).show()
        }

        findViewById<ImageButton>(R.id.btnDelete1).setOnClickListener {
            Toast.makeText(this, "Delete Lab 101 - Coming soon", Toast.LENGTH_SHORT).show()
        }

        // Room 2 actions
        findViewById<ImageButton>(R.id.btnEdit2).setOnClickListener {
            Toast.makeText(this, "Edit Lecture Hall A - Coming soon", Toast.LENGTH_SHORT).show()
        }

        findViewById<ImageButton>(R.id.btnDelete2).setOnClickListener {
            Toast.makeText(this, "Delete Lecture Hall A - Coming soon", Toast.LENGTH_SHORT).show()
        }

        // FAB - Add room
        findViewById<FloatingActionButton>(R.id.fabAddRoom).setOnClickListener {
            Toast.makeText(this, "Add new room - Coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun displayUserInfo() {
        val prefs = getSharedPreferences("user_session", MODE_PRIVATE)
        val fullName = prefs.getString("full_name", "Admin") ?: "Admin"

        Log.d("AdminDashboard", "Admin user: $fullName")
        Toast.makeText(this, "Welcome, $fullName", Toast.LENGTH_SHORT).show()
    }

    private fun loadDashboardData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("AdminDashboard", "→ Fetching dashboard data")

                val request = Request.Builder()
                    .url("$BACKEND_URL/get_admin_dashboard.php")
                    .build()

                val response = client.newCall(request).execute()
                val jsonData = response.body?.string() ?: ""

                Log.d("AdminDashboard", "← Response Code: ${response.code}")
                Log.d("AdminDashboard", "← Response Body: $jsonData")

                val json = JSONObject(jsonData)
                val success = json.getBoolean("success")

                if (success) {
                    val stats = json.getJSONObject("stats")
                    val totalTeachers = stats.getInt("total_teachers")
                    val totalRooms = stats.getInt("total_rooms")
                    val totalSchedules = stats.getInt("total_schedules")

                    Log.d("AdminDashboard", "✓ Stats - Teachers: $totalTeachers, Rooms: $totalRooms, Schedules: $totalSchedules")

                    withContext(Dispatchers.Main) {
                        // Update UI with stats
                        findViewById<TextView>(R.id.tvPendingCount)?.text =
                            "$totalSchedules schedules in system"

                        Toast.makeText(this@AdminDashboardActivity,
                            "Dashboard loaded successfully", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val message = json.optString("message", "Failed to load dashboard")
                    Log.e("AdminDashboard", "✗ API error: $message")

                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@AdminDashboardActivity,
                            "Error: $message", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("AdminDashboard", "✗ Error loading dashboard: ${e.message}", e)

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AdminDashboardActivity,
                        "Error: ${e.message}", Toast.LENGTH_LONG).show()
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
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}