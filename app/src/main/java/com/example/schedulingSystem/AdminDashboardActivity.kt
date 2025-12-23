package com.example.schedulingSystem

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.schedulingSystem.adapters.AdminRoomScheduleAdapter
import com.example.schedulingSystem.models.RoomItem
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class AdminDashboardActivity : AppCompatActivity() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val BACKEND_URL = "http://10.0.2.2/scheduling-api"
    }

    // UI References
    private lateinit var roomAdapter: AdminRoomScheduleAdapter
    private lateinit var rvRooms: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // === Security Check ===
        val prefs = getSharedPreferences("user_session", MODE_PRIVATE)
        val isLoggedIn = prefs.getBoolean("is_logged_in", false)
        val accountType = prefs.getString("account_type", "")

        if (!isLoggedIn || accountType?.lowercase() != "admin") {
            Log.w("AdminDashboard", "Access denied or not logged in")
            Toast.makeText(this, "Access denied: Admin only", Toast.LENGTH_LONG).show()
            redirectToLogin()
            return
        }

        setContentView(R.layout.activity_admin_dashboard)

        // Initialize RecyclerView
        rvRooms = findViewById(R.id.containerRooms) // Make sure this ID matches your layout!
        roomAdapter = AdminRoomScheduleAdapter { room ->
            // FIXED: Navigate to the correct activity
            val intent = Intent(this@AdminDashboardActivity, AdminDashboardScheduleRoom::class.java)
            intent.putExtra("room_id", room.roomId)
            intent.putExtra("room_name", room.roomName)
            startActivity(intent)
        }

        rvRooms.apply {
            layoutManager = LinearLayoutManager(this@AdminDashboardActivity)
            adapter = roomAdapter
        }

        setupClickListeners()
        displayUserInfo()
        loadDashboardData()
        loadRoomsFromApi()
    }

    private fun setupClickListeners() {
        // Settings → Logout
        findViewById<ImageButton>(R.id.btnSettings).setOnClickListener { performLogout() }
        findViewById<MaterialButton>(R.id.btnReview).setOnClickListener {
            Toast.makeText(this, "Review pending approvals - Coming soon", Toast.LENGTH_SHORT).show()
        }

        // === TAB NAVIGATION ===
        val tabSchedules = findViewById<TextView>(R.id.tabSchedules)
        val tabUsers = findViewById<TextView>(R.id.tabUsers)
        val tabRooms = findViewById<TextView>(R.id.tabRooms)

        tabSchedules.setOnClickListener {
            updateTabSelection(tabSchedules, tabUsers, tabRooms)
            Toast.makeText(this, "Schedules Dashboard", Toast.LENGTH_SHORT).show()
        }

        tabUsers.setOnClickListener {
            updateTabSelection(tabUsers, tabSchedules, tabRooms)
            startActivity(Intent(this, AdminManageUsersActivity::class.java))

        }

        tabRooms.setOnClickListener {
            try {
                updateTabSelection(tabRooms, tabSchedules, tabUsers)
                val intent = Intent(this@AdminDashboardActivity, AdminManageRoomsActivity::class.java)
                startActivity(intent)
            } catch (e: Exception) {
                Log.e("AdminDashboard", "Error navigating to Manage Rooms", e)
                Toast.makeText(this@AdminDashboardActivity, "Error opening Manage Rooms: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        // Default: highlight Schedules tab
        updateTabSelection(tabSchedules, tabUsers, tabRooms)
    }

    private fun updateTabSelection(selected: TextView, vararg others: TextView) {
        selected.apply {
            setBackgroundResource(R.drawable.bg_input_outline)
            backgroundTintList = ContextCompat.getColorStateList(this@AdminDashboardActivity, R.color.white)
            setTextColor(ContextCompat.getColor(this@AdminDashboardActivity, R.color.primary_dark_green))
            setTypeface(null, android.graphics.Typeface.BOLD)
            elevation = 4f
        }

        others.forEach { tab ->
            tab.apply {
                background = null
                backgroundTintList = null
                setTextColor(ContextCompat.getColor(this@AdminDashboardActivity, R.color.white))
                setTypeface(null, android.graphics.Typeface.NORMAL)
                elevation = 0f
            }
        }
    }

    private fun displayUserInfo() {
        val fullName = getSharedPreferences("user_session", MODE_PRIVATE)
            .getString("full_name", "Admin") ?: "Admin"
        Toast.makeText(this, "Welcome, $fullName", Toast.LENGTH_SHORT).show()
    }

    private fun loadDashboardData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = client.newCall(
                    Request.Builder()
                        .url("$BACKEND_URL/get_admin_dashboard_details.php")
                        .build()
                ).execute()

                val json = JSONObject(response.body?.string() ?: "")
                if (json.getBoolean("success")) {
                    val stats = json.getJSONObject("stats")
                    withContext(Dispatchers.Main) {
                        findViewById<TextView>(R.id.tvPendingCount)?.text =
                            "${stats.getInt("total_schedules")} schedules in system"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AdminDashboardActivity, "Stats error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun loadRoomsFromApi() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("AdminDashboard", "Fetching rooms from API...")
                val response = client.newCall(
                    Request.Builder()
                        .url("$BACKEND_URL/get_rooms.php")
                        .build()
                ).execute()

                val jsonData = response.body?.string() ?: ""
                Log.d("AdminDashboard", "Raw response: $jsonData")

                val json = JSONObject(jsonData)
                if (json.getBoolean("success")) {
                    val roomsArray = json.getJSONArray("rooms")
                    val roomList = mutableListOf<RoomItem>()

                    for (i in 0 until roomsArray.length()) {
                        val obj = roomsArray.getJSONObject(i)
                        roomList.add(
                            RoomItem(
                                roomId = obj.getInt("id"),
                                roomName = obj.getString("name"),
                                roomCapacity = obj.getInt("capacity"),
                                status = "Available",
                                isAvailable = true
                            )
                        )
                    }

                    withContext(Dispatchers.Main) {
                        roomAdapter.submitList(roomList)
                        Toast.makeText(
                            this@AdminDashboardActivity,
                            "Loaded ${roomList.size} rooms",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@AdminDashboardActivity,
                            "API Error: ${json.optString("message", "Unknown error")}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("AdminDashboard", "Failed to load rooms", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@AdminDashboardActivity,
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
        startActivity(
            Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        )
        finish()
    }
}