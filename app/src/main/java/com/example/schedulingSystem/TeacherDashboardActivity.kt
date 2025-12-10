package com.example.schedulingSystem

import com.example.schedulingSystem.models.RoomAvailability
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class TeacherDashboardActivity : AppCompatActivity() {

    private lateinit var rvRooms: RecyclerView
    private lateinit var tvProfName: TextView
    private lateinit var tvGreeting: TextView
    private lateinit var btnSettings: ImageView
    private lateinit var tvListHeader: TextView

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val roomAdapter = TeacherRoomAdapter()
    private var currentView = "day" // "day" or "week"

    companion object {
        private const val BACKEND_URL = "http://10.0.2.2/scheduling-api"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if user is logged in and is teacher
        val prefs = getSharedPreferences("user_session", MODE_PRIVATE)
        val isLoggedIn = prefs.getBoolean("is_logged_in", false)
        val accountType = prefs.getString("account_type", "")

        if (!isLoggedIn) {
            Log.w("TeacherDashboard", "User not logged in, redirecting to login")
            redirectToLogin()
            return
        }

        if (accountType?.lowercase() != "teacher") {
            Log.w("TeacherDashboard", "User is not teacher (type: $accountType), redirecting to login")
            redirectToLogin()
            return
        }

        setContentView(R.layout.activity_teacher_dashboard)

        initViews()
        setupClickListeners()
        updateUserName()
        setupRecyclerView()
        loadRoomsFromApi()
    }

    private fun initViews() {
        rvRooms = findViewById(R.id.rvRooms)
        tvProfName = findViewById(R.id.tvProfName)
        tvGreeting = findViewById(R.id.tvGreeting)
        btnSettings = findViewById(R.id.btnSettings)
        tvListHeader = findViewById(R.id.tvListHeader)
    }

    private fun setupClickListeners() {
        btnSettings.setOnClickListener {
            performLogout()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateUserName() {
        val prefs = getSharedPreferences("user_session", MODE_PRIVATE)
        val fullName = prefs.getString("full_name", "Teacher") ?: "Teacher"
        tvGreeting.text = "Welcome back,"
        tvProfName.text = fullName
    }

    private fun setupRecyclerView() {
        rvRooms.layoutManager = LinearLayoutManager(this)
        rvRooms.adapter = roomAdapter
    }

    private fun loadRoomsFromApi() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Get current date
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val currentDate = dateFormat.format(Date())

                // Build URL with view type parameter
                val url = if (currentView == "day") {
                    "$BACKEND_URL/get_rooms_schedule.php?view=day&date=$currentDate"
                } else {
                    "$BACKEND_URL/get_rooms_schedule.php?view=week&date=$currentDate"
                }

                val request = Request.Builder()
                    .url(url)
                    .build()

                val response = client.newCall(request).execute()
                val jsonData = response.body?.string() ?: ""

                if (jsonData.isEmpty()) {
                    Log.w("TeacherDashboard", "Empty response from get_rooms_schedule.php")
                    return@launch
                }

                val json = JSONObject(jsonData)
                val success = json.optBoolean("success", false)

                if (success) {
                    val roomsArray = json.optJSONArray("rooms")
                    if (roomsArray != null) {
                        val list = mutableListOf<RoomAvailability>()

                        for (i in 0 until roomsArray.length()) {
                            try {
                                val obj = roomsArray.getJSONObject(i)
                                list.add(
                                    RoomAvailability(
                                        roomId = obj.getInt("room_ID"),
                                        roomName = obj.getString("room_name"),
                                        roomCapacity = obj.getInt("room_capacity"),
                                        status = obj.optString("status", "Available"),
                                        isAvailable = obj.optBoolean("isAvailable", true)
                                    )
                                )
                            } catch (e: Exception) {
                                Log.e("TeacherDashboard", "Error parsing room at index $i: ${e.message}")
                            }
                        }

                        withContext(Dispatchers.Main) {
                            roomAdapter.submitList(list)
                            Log.d("TeacherDashboard", "✓ Loaded ${list.size} rooms for $currentView view")
                        }
                    } else {
                        Log.w("TeacherDashboard", "No rooms array in response")
                    }
                } else {
                    val message = json.optString("message", "Failed to load rooms")
                    Log.e("TeacherDashboard", "✗ API error: $message")
                }
            } catch (e: Exception) {
                Log.e("TeacherDashboard", "✗ Error loading rooms: ${e.message}", e)
            }
        }
    }

    private fun performLogout() {
        getSharedPreferences("user_session", MODE_PRIVATE).edit { clear() }
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun redirectToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}