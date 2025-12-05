package com.example.schedulingSystem

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
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
        private const val TAG = "TeacherDashboard"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "onCreate started")

        // Check if user is logged in
        val prefs = getSharedPreferences("user_session", MODE_PRIVATE)
        val isLoggedIn = prefs.getBoolean("is_logged_in", false)

        if (!isLoggedIn) {
            Log.w(TAG, "User not logged in, redirecting")
            redirectToLogin()
            return
        }

        try {
            setContentView(R.layout.activity_teacher_dashboard)
            Log.d(TAG, "Layout inflated successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error inflating layout", e)
            Toast.makeText(this, "Error loading dashboard: ${e.message}", Toast.LENGTH_LONG).show()
            redirectToLogin()
            return
        }

        initViews()
        setupDrawer()
        updateUserName()
        setupRecyclerView()
        displayUserInfo()

        // Load rooms with mock data first, then try API
        loadMockRooms()
        loadRoomsFromApi()
    }

    private fun initViews() {
        try {
            drawerLayout = findViewById(R.id.drawerLayout)
            navView = findViewById(R.id.navView)
            rvRooms = findViewById(R.id.rvRooms)
            tvProfName = findViewById(R.id.tvProfName)
            tvGreeting = findViewById(R.id.tvGreeting)
            btnOpenDrawer = findViewById(R.id.btnOpenDrawer)
            Log.d(TAG, "All views initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing views", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            throw e
        }
    }

    private fun setupDrawer() {
        btnOpenDrawer.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        navView.setNavigationItemSelectedListener(this)
        Log.d(TAG, "Drawer setup complete")
    }

    @SuppressLint("SetTextI18n")
    private fun updateUserName() {
        val prefs = getSharedPreferences("user_session", MODE_PRIVATE)
        val fullName = prefs.getString("full_name", "Teacher") ?: "Teacher"

        tvGreeting.text = "Welcome back,"
        tvProfName.text = fullName

        // Update Navigation Drawer header
        try {
            val headerView = navView.getHeaderView(0)
            headerView.findViewById<TextView>(R.id.tvNavName)?.text = fullName
            headerView.findViewById<TextView>(R.id.tvNavEmail)?.text = prefs.getString("username", "")
            Log.d(TAG, "User info updated: $fullName")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating nav header", e)
        }
    }

    private fun displayUserInfo() {
        val prefs = getSharedPreferences("user_session", MODE_PRIVATE)
        val fullName = prefs.getString("full_name", "Teacher") ?: "Teacher"
        val accountType = prefs.getString("account_type", "Unknown")

        Log.d(TAG, "Teacher logged in: $fullName (Type: $accountType)")
        Toast.makeText(this, "Welcome, $fullName", Toast.LENGTH_SHORT).show()
    }

    private fun setupRecyclerView() {
        rvRooms.layoutManager = LinearLayoutManager(this)
        rvRooms.adapter = roomAdapter
        Log.d(TAG, "RecyclerView setup complete")
    }

    private fun loadMockRooms() {
        val mockRooms = listOf(
            RoomItem(1, "Computer Lab 1", 40, "Available", true),
            RoomItem(2, "Lecture Hall A", 100, "Occupied", false),
            RoomItem(3, "Science Lab", 30, "Available", true)
        )
        roomAdapter.submitList(mockRooms)
        Log.d(TAG, "Mock rooms loaded")
    }

    private fun loadRoomsFromApi() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "→ Fetching rooms from API")

                val request = Request.Builder()
                    .url("http://10.0.2.2/scheduling-api/get_rooms.php")
                    .build()

                val response = client.newCall(request).execute()
                val jsonData = response.body?.string() ?: ""

                Log.d(TAG, "← API Response: ${response.code}")

                if (response.isSuccessful && jsonData.isNotEmpty()) {
                    val json = JSONObject(jsonData)
                    val success = json.optBoolean("success", false)

                    if (success) {
                        val roomsArray = json.getJSONArray("rooms")
                        val roomList = mutableListOf<RoomItem>()

                        for (i in 0 until roomsArray.length()) {
                            val obj = roomsArray.getJSONObject(i)
                            roomList.add(
                                RoomItem(
                                    id = obj.getInt("id"),
                                    name = obj.getString("name"),
                                    capacity = obj.getInt("capacity"),
                                    status = obj.getString("status"),
                                    isAvailable = obj.getBoolean("isAvailable")
                                )
                            )
                        }

                        withContext(Dispatchers.Main) {
                            roomAdapter.submitList(roomList)
                            Log.d(TAG, "✓ Loaded ${roomList.size} rooms from API")
                        }
                    } else {
                        Log.w(TAG, "API returned success=false")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading rooms from API", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@TeacherDashboardActivity,
                        "Using offline data",
                        Toast.LENGTH_SHORT
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

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}