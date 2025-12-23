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
import com.example.schedulingSystem.adapters.AdminRoomAdapter
import com.example.schedulingSystem.models.RoomItem
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class AdminManageRoomsActivity : AppCompatActivity() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val BACKEND_URL = "http://10.0.2.2/scheduling-api"
        private const val TAG = "AdminManageRooms"
    }

    private lateinit var roomAdapter: AdminRoomAdapter
    private lateinit var rvRooms: RecyclerView
    private var loadJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            setContentView(R.layout.activity_admin_manage_rooms)

            // Security check
            val prefs = getSharedPreferences("user_session", MODE_PRIVATE)
            val isLoggedIn = prefs.getBoolean("is_logged_in", false)
            val accountType = prefs.getString("account_type", "")

            if (!isLoggedIn || accountType?.lowercase() != "admin") {
                Toast.makeText(this, "Access denied: Admin only", Toast.LENGTH_LONG).show()
                redirectToLogin()
                return
            }

            // Setup RecyclerView with error handling
            setupRecyclerView()
            setupClickListeners()
            loadRoomsFromApi()

        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
            Toast.makeText(this, "Error initializing activity: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun setupRecyclerView() {
        try {
            rvRooms = findViewById(R.id.containerRooms)

            // Check if RecyclerView exists
            if (rvRooms == null) {
                throw IllegalStateException("RecyclerView with id 'containerRooms' not found in layout")
            }

            roomAdapter = AdminRoomAdapter(this, ::loadRoomsFromApi)
            rvRooms.apply {
                layoutManager = LinearLayoutManager(this@AdminManageRoomsActivity)
                adapter = roomAdapter
                // Add item animator to prevent crash on updates
                itemAnimator = null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up RecyclerView", e)
            throw e // Re-throw to be caught in onCreate
        }
    }

    private fun setupClickListeners() {
        try {
            // Settings → Logout
            findViewById<ImageButton>(R.id.btnSettings)?.setOnClickListener { performLogout() }

            // Tab Navigation
            val tabSchedules = findViewById<TextView>(R.id.tabSchedules)
            val tabUsers = findViewById<TextView>(R.id.tabUsers)
            val tabRooms = findViewById<TextView>(R.id.tabRooms)

            // Verify all tabs exist
            if (tabSchedules == null || tabUsers == null || tabRooms == null) {
                Log.e(TAG, "One or more tab views not found in layout")
                return
            }

            tabSchedules.setOnClickListener {
                updateTabSelection(tabSchedules, tabUsers, tabRooms)
                startActivity(Intent(this, AdminDashboardActivity::class.java))
                finish()
            }

            tabUsers.setOnClickListener {
                updateTabSelection(tabUsers, tabSchedules, tabRooms)
                startActivity(Intent(this, AdminManageUsersActivity::class.java))
                finish()
            }

            tabRooms.setOnClickListener {
                updateTabSelection(tabRooms, tabSchedules, tabUsers)
                // Already on this tab
            }

            // FAB to Add Room
            findViewById<ImageButton>(R.id.btnMain)?.setOnClickListener {
                Toast.makeText(this, "Add New Room - Coming soon", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error setting up click listeners", e)
        }
    }

    private fun loadRoomsFromApi() {
        // Cancel any existing load job
        loadJob?.cancel()

        loadJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "Fetching rooms...")

                val request = Request.Builder()
                    .url("$BACKEND_URL/get_rooms.php")
                    .build()

                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@AdminManageRoomsActivity,
                            "HTTP Error: ${response.code}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    return@launch
                }

                val jsonData = response.body?.string() ?: ""
                Log.d(TAG, "Response: $jsonData")

                if (jsonData.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@AdminManageRoomsActivity,
                            "Empty response from server",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    return@launch
                }

                val json = JSONObject(jsonData)

                if (!json.optBoolean("success", false)) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@AdminManageRoomsActivity,
                            "API Error: ${json.optString("message", "Unknown error")}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    return@launch
                }

                val roomsArray = json.optJSONArray("rooms")

                if (roomsArray == null) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@AdminManageRoomsActivity,
                            "No rooms data found",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    return@launch
                }

                val roomList = mutableListOf<RoomItem>()

                for (i in 0 until roomsArray.length()) {
                    try {
                        val obj = roomsArray.getJSONObject(i)

                        // Safely extract values with defaults
                        val roomId = obj.optInt("id", -1)
                        val roomName = obj.optString("name", "Unknown Room")
                        val roomCapacity = obj.optInt("capacity", 0)

                        if (roomId != -1) {
                            roomList.add(
                                RoomItem(
                                    roomId = roomId,
                                    roomName = roomName,
                                    roomCapacity = roomCapacity,
                                    status = "Available",
                                    isAvailable = true
                                )
                            )
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing room at index $i", e)
                        // Continue to next room instead of failing completely
                    }
                }

                withContext(Dispatchers.Main) {
                    try {
                        if (!isFinishing && !isDestroyed) {
                            roomAdapter.submitList(roomList)
                            Toast.makeText(
                                this@AdminManageRoomsActivity,
                                "Loaded ${roomList.size} rooms",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error updating adapter", e)
                        if (!isFinishing) {
                            Toast.makeText(
                                this@AdminManageRoomsActivity,
                                "Error displaying rooms: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Failed to load rooms", e)
                withContext(Dispatchers.Main) {
                    if (!isFinishing) {
                        Toast.makeText(
                            this@AdminManageRoomsActivity,
                            "Network error: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
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
                redirectToLogin()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun redirectToLogin() {
        try {
            startActivity(
                Intent(this, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
            )
            finish()
        } catch (e: Exception) {
            Log.e(TAG, "Error redirecting to login", e)
        }
    }

    private fun updateTabSelection(selected: TextView, vararg others: TextView) {
        try {
            selected.apply {
                setBackgroundResource(R.drawable.bg_input_outline)
                backgroundTintList = ContextCompat.getColorStateList(
                    this@AdminManageRoomsActivity,
                    R.color.white
                )
                setTextColor(ContextCompat.getColor(
                    this@AdminManageRoomsActivity,
                    R.color.primary_dark_green
                ))
                setTypeface(null, android.graphics.Typeface.BOLD)
                elevation = 4f
            }

            others.forEach { tab ->
                tab.apply {
                    background = null
                    backgroundTintList = null
                    setTextColor(ContextCompat.getColor(
                        this@AdminManageRoomsActivity,
                        R.color.white
                    ))
                    setTypeface(null, android.graphics.Typeface.NORMAL)
                    elevation = 0f
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating tab selection", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cancel any ongoing network operations
        loadJob?.cancel()
    }
}