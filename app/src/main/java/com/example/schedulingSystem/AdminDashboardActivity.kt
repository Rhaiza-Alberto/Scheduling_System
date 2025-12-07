package com.example.schedulingSystem

import android.content.Intent
import android.os.Bundle
import android.util.Log
//import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.schedulingSystem.adapters.AdminRoomScheduleAdapter
import com.example.schedulingSystem.models.RoomItem
import com.google.android.material.button.MaterialButton
// import com.google.android.material.floatingactionbutton.FloatingActionButton  // ← Commented out
// import android.widget.LinearLayout                                      // ← Still needed elsewhere
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import androidx.core.content.ContextCompat
import com.example.schedulingSystem.AdminManageRoomsActivity
import com.example.schedulingSystem.AdminManageUsersActivity

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

    // private var isFabMenuOpen = false   // ← FAB removed

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

        // Initialize RecyclerView (this is the important part!)
        rvRooms = findViewById(R.id.containerRooms)
        roomAdapter = AdminRoomScheduleAdapter()
        rvRooms.apply {
            layoutManager = LinearLayoutManager(this@AdminDashboardActivity)
            adapter = roomAdapter
        }

        setupClickListeners()      // ← only settings + review button now
        displayUserInfo()
        loadDashboardData()
        loadRoomsFromApi()         // ← This will show your real rooms!
    }

    private fun setupClickListeners() {
        // Settings → Logout
        findViewById<ImageButton>(R.id.btnSettings).setOnClickListener { performLogout() }

        // Review button
        findViewById<MaterialButton>(R.id.btnReview).setOnClickListener {
            Toast.makeText(this, "Review pending approvals - Coming soon", Toast.LENGTH_SHORT).show()
        }

        // === TAB NAVIGATION ===
        val tabSchedules = findViewById<TextView>(R.id.tabSchedules)
        val tabUsers = findViewById<TextView>(R.id.tabUsers)
        val tabRooms = findViewById<TextView>(R.id.tabRooms)

        // Schedules Tab (Current Dashboard - highlight active)
        tabSchedules.setOnClickListener {
            // Already here - just update visual state
            updateTabSelection(tabSchedules, tabUsers, tabRooms)
            Toast.makeText(this, "Schedules Dashboard", Toast.LENGTH_SHORT).show()
        }

        tabUsers.setOnClickListener {
            updateTabSelection(tabUsers, tabSchedules, tabRooms)
            startActivity(Intent(this, AdminManageUsersActivity::class.java))
            finish()
        }
        tabRooms.setOnClickListener {
            updateTabSelection(tabRooms, tabSchedules, tabUsers)
            startActivity(Intent(this, AdminManageRoomsActivity::class.java))
            finish()
        }

        updateTabSelection(tabSchedules, tabUsers, tabRooms)
    }

    // Helper: Highlight selected tab
    private fun updateTabSelection(selected: TextView, vararg others: TextView) {
        // Selected tab → White background + Green text + Bold
        selected.apply {
            setBackgroundResource(R.drawable.bg_input_outline)
            backgroundTintList = ContextCompat.getColorStateList(this@AdminDashboardActivity, R.color.white)
            setTextColor(ContextCompat.getColor(this@AdminDashboardActivity, R.color.primary_dark_green))
            setTypeface(null, android.graphics.Typeface.BOLD)
            elevation = 4f  // Slight shadow
        }

        // All other tabs → Transparent + White text + Normal weight
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

    // FAB open/close functions also commented out
//    private fun openFabMenu(...) { ... }
//    private fun closeFabMenu(...) { ... }

    private fun displayUserInfo() {
        val fullName = getSharedPreferences("user_session", MODE_PRIVATE)
            .getString("full_name", "Admin") ?: "Admin"
        Toast.makeText(this, "Welcome, $fullName", Toast.LENGTH_SHORT).show()
    }

    private fun loadDashboardData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = client.newCall(Request.Builder()
                    .url("$BACKEND_URL/get_admin_dashboard_details.php").build()).execute()
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
                Log.d("AdminDashboard", "Response: $jsonData")

                val json = JSONObject(jsonData)
                if (json.getBoolean("success")) {
                    val roomsArray = json.getJSONArray("rooms")
                    val roomList = mutableListOf<RoomItem>()

                    for (i in 0 until roomsArray.length()) {
                        val obj = roomsArray.getJSONObject(i)

                        roomList.add(
                            RoomItem(
                                roomId = obj.getInt("id"),           // matches "id"
                                roomName = obj.getString("name"),    // matches "name"
                                roomCapacity = obj.getInt("capacity"), // matches "capacity"
                                status = "Available",                // default (optional)
                                isAvailable = true                   // default (optional)
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
                Log.e("AdminDashboard", "Room load error", e)
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
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}