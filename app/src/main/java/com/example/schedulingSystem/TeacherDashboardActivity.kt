package com.example.schedulingSystem

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class TeacherDashboardActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var rvRooms: RecyclerView
    private lateinit var tvProfName: TextView
    private lateinit var tvGreeting: TextView
    private lateinit var btnOpenDrawer: ImageView

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val roomAdapter = TeacherRoomAdapter()

    companion object {
        private const val BACKEND_URL = "http://10.0.2.2/scheduling-api"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if user is logged in
        val prefs = getSharedPreferences("user_session", MODE_PRIVATE)
        val isLoggedIn = prefs.getBoolean("is_logged_in", false)

        if (!isLoggedIn) {
            Log.w("TeacherDashboard", "User not logged in, redirecting to login")
            redirectToLogin()
            return
        }

        try {
            setContentView(R.layout.activity_teacher_dashboard)

            initViews()
            setupDrawer()
            setupBackPressHandler()
            updateUserName()
            setupRecyclerView()
            loadRoomsFromApi()

        } catch (e: Exception) {
            Log.e("TeacherDashboard", "Error in onCreate: ${e.message}", e)
            Toast.makeText(this, "Error loading dashboard: ${e.message}", Toast.LENGTH_LONG).show()
            redirectToLogin()
        }
    }

    private fun initViews() {
        try {
            drawerLayout = findViewById(R.id.drawerLayout)
            navView = findViewById(R.id.navView)
            rvRooms = findViewById(R.id.rvRooms)
            tvProfName = findViewById(R.id.tvProfName)
            tvGreeting = findViewById(R.id.tvGreeting)
            btnOpenDrawer = findViewById(R.id.btnOpenDrawer)

            Log.d("TeacherDashboard", "✓ All views initialized successfully")
        } catch (e: Exception) {
            Log.e("TeacherDashboard", "✗ Error initializing views: ${e.message}", e)
            throw e
        }
    }

    private fun setupDrawer() {
        btnOpenDrawer.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        navView.setNavigationItemSelectedListener(this)

        // Update drawer header with user info
        updateDrawerHeader()
    }

    private fun setupBackPressHandler() {
        // Modern way to handle back press - NO WARNING
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    // Let the system handle back press (will close the activity)
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun updateUserName() {
        val prefs = getSharedPreferences("user_session", MODE_PRIVATE)
        val fullName = prefs.getString("full_name", "Teacher") ?: "Teacher"

        Log.d("TeacherDashboard", "Loading user: $fullName")

        tvGreeting.text = "Welcome back,"
        tvProfName.text = fullName

        Toast.makeText(this, "Welcome, $fullName!", Toast.LENGTH_SHORT).show()
    }

    private fun updateDrawerHeader() {
        val prefs = getSharedPreferences("user_session", MODE_PRIVATE)
        val fullName = prefs.getString("full_name", "Teacher") ?: "Teacher"
        val username = prefs.getString("username", "") ?: ""

        try {
            val headerView = navView.getHeaderView(0)
            headerView.findViewById<TextView>(R.id.tvNavName)?.text = fullName
            headerView.findViewById<TextView>(R.id.tvNavEmail)?.text = username
        } catch (e: Exception) {
            Log.e("TeacherDashboard", "Error updating drawer header: ${e.message}")
        }
    }

    private fun setupRecyclerView() {
        rvRooms.layoutManager = LinearLayoutManager(this)
        rvRooms.adapter = roomAdapter
    }

    private fun loadRoomsFromApi() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("TeacherDashboard", "→ Fetching rooms data")

                val request = Request.Builder()
                    .url("$BACKEND_URL/get_rooms.php")
                    .build()

                val response = client.newCall(request).execute()
                val jsonData = response.body?.string() ?: ""

                Log.d("TeacherDashboard", "← Response Code: ${response.code}")
                Log.d("TeacherDashboard", "← Response Body: $jsonData")

                if (jsonData.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@TeacherDashboardActivity,
                            "Empty response from server",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    return@launch
                }

                val json = JSONObject(jsonData)
                val success = json.getBoolean("success")

                if (success) {
                    val roomsArray = json.getJSONArray("rooms")
                    val roomList = mutableListOf<RoomAvailability>()

                    for (i in 0 until roomsArray.length()) {
                        val obj = roomsArray.getJSONObject(i)
                        roomList.add(
                            RoomAvailability(
                                roomId = obj.getInt("room_ID"),
                                roomName = obj.getString("room_name"),
                                roomCapacity = obj.getInt("room_capacity"),
                                status = obj.optString("status", "Available"),
                                isAvailable = obj.optBoolean("isAvailable", true)
                            )
                        )
                    }

                    Log.d("TeacherDashboard", "✓ Loaded ${roomList.size} rooms")

                    withContext(Dispatchers.Main) {
                        roomAdapter.submitList(roomList)
                        Toast.makeText(
                            this@TeacherDashboardActivity,
                            "Loaded ${roomList.size} rooms",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    val message = json.optString("message", "Failed to load rooms")
                    Log.e("TeacherDashboard", "✗ API error: $message")

                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@TeacherDashboardActivity,
                            "Error: $message",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("TeacherDashboard", "✗ Error loading rooms: ${e.message}", e)

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@TeacherDashboardActivity,
                        "Network error: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                Toast.makeText(this, "Dashboard", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_my_requests -> {
                Toast.makeText(this, "My Requests - Coming soon", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_profile -> {
                Toast.makeText(this, "Profile - Coming soon", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_logout -> {
                performLogout()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
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

/**
 * Data class for room availability - specific to TeacherDashboard
 */
data class RoomAvailability(
    val roomId: Int,
    val roomName: String,
    val roomCapacity: Int,
    val status: String,
    val isAvailable: Boolean
)