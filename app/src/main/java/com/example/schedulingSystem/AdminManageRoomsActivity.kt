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
    }

    private lateinit var roomAdapter: AdminRoomAdapter
    private lateinit var rvRooms: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        // Setup RecyclerView
        rvRooms = findViewById(R.id.containerRooms)
        roomAdapter = AdminRoomAdapter(this, ::loadRoomsFromApi)
        rvRooms.apply {
            layoutManager = LinearLayoutManager(this@AdminManageRoomsActivity)
            adapter = roomAdapter
        }

        setupClickListeners()
        loadRoomsFromApi()
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
            updateTabSelection(tabUsers, tabSchedules, tabRooms)
            startActivity(Intent(this, AdminManageUsersActivity::class.java))
            finish()

        }

        tabRooms.setOnClickListener {
            updateTabSelection(tabRooms, tabSchedules, tabUsers)
            Toast.makeText(this, "Manage Users", Toast.LENGTH_SHORT).show()

        }

        // Back to Dashboard (optional: you can add a back button later)
        // Or just press back key

        // FAB to Add Room (Coming soon)
        findViewById<ImageButton>(R.id.btnMain).setOnClickListener {
            Toast.makeText(this, "Add New Room - Coming soon", Toast.LENGTH_LONG).show()
        }
    }


    private fun loadRoomsFromApi() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("ManageRooms", "Fetching rooms...")
                val response = client.newCall(
                    Request.Builder()
                        .url("$BACKEND_URL/get_rooms.php")
                        .build()
                ).execute()

                val jsonData = response.body?.string() ?: ""
                Log.d("ManageRooms", "Response: $jsonData")

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
                                roomCapacity = obj.getInt("capacity")

                            )
                        )
                    }

                    withContext(Dispatchers.Main) {
                        roomAdapter.submitList(roomList)
                        Toast.makeText(
                            this@AdminManageRoomsActivity,
                            "Loaded ${roomList.size} rooms",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@AdminManageRoomsActivity,
                            "Error: ${json.optString("message")}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("ManageRooms", "Load error", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@AdminManageRoomsActivity,
                        "Network error: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun redirectToLogin() {
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
    private fun updateTabSelection(selected: TextView, vararg others: TextView) {
        selected.apply {
            setBackgroundResource(R.drawable.bg_input_outline)
            backgroundTintList = ContextCompat.getColorStateList(this@AdminManageRoomsActivity, R.color.white)
            setTextColor(ContextCompat.getColor(this@AdminManageRoomsActivity, R.color.primary_dark_green))
            setTypeface(null, android.graphics.Typeface.BOLD)
            elevation = 4f
        }

        others.forEach { tab ->
            tab.apply {
                background = null
                backgroundTintList = null
                setTextColor(ContextCompat.getColor(this@AdminManageRoomsActivity, R.color.white))
                setTypeface(null, android.graphics.Typeface.NORMAL)
                elevation = 0f
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

}
