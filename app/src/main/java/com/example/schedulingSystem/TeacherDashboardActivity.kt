package com.example.schedulingSystem

import com.example.schedulingSystem.models.RoomAvailability
import com.example.schedulingSystem.models.TeacherScheduleItem
import com.example.schedulingSystem.adapters.TeacherScheduleAdapter
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class TeacherDashboardActivity : AppCompatActivity() {

    private lateinit var rvRooms: RecyclerView
    private lateinit var rvMySchedule: RecyclerView
    private lateinit var tvProfName: TextView
    private lateinit var tvGreeting: TextView
    private lateinit var btnSettings: ImageView
    private lateinit var btnDay: MaterialButton
    private lateinit var btnWeek: MaterialButton
    private lateinit var tvListHeader: TextView
    private lateinit var tabMySchedule: TextView
    private lateinit var tabRoomSchedules: TextView
    private lateinit var myScheduleContainer: View
    private lateinit var roomScheduleContainer: View

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val roomAdapter = TeacherRoomAdapter()
    private val scheduleAdapter = TeacherScheduleAdapter()
    private var currentView = "day" // "day" or "week"
    private var currentTab = "schedule" // "schedule" or "rooms"

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
        setupRecyclerViews()

        // Load my schedule by default
        showMySchedule()
    }

    private fun initViews() {
        rvRooms = findViewById(R.id.rvRooms)
        rvMySchedule = findViewById(R.id.rvMySchedule)
        tvProfName = findViewById(R.id.tvProfName)
        tvGreeting = findViewById(R.id.tvGreeting)
        btnSettings = findViewById(R.id.btnSettings)
        btnDay = findViewById(R.id.btnDay)
        btnWeek = findViewById(R.id.btnWeek)
        tvListHeader = findViewById(R.id.tvListHeader)
        tabMySchedule = findViewById(R.id.tabMySchedule)
        tabRoomSchedules = findViewById(R.id.tabRoomSchedules)
        myScheduleContainer = findViewById(R.id.myScheduleContainer)
        roomScheduleContainer = findViewById(R.id.roomScheduleContainer)
    }

    private fun setupClickListeners() {
        btnSettings.setOnClickListener {
            performLogout()
        }

        // Tab switching
        tabMySchedule.setOnClickListener {
            showMySchedule()
        }

        tabRoomSchedules.setOnClickListener {
            showRoomSchedules()
        }

        // Day/Week toggle
        btnDay.setOnClickListener {
            if (currentView != "day") {
                currentView = "day"
                updateViewToggle()
                if (currentTab == "schedule") {
                    loadMySchedule()
                } else {
                    loadRoomsFromApi()
                }
            }
        }

        btnWeek.setOnClickListener {
            if (currentView != "week") {
                currentView = "week"
                updateViewToggle()
                if (currentTab == "schedule") {
                    loadMySchedule()
                } else {
                    loadRoomsFromApi()
                }
            }
        }
    }

    private fun showMySchedule() {
        currentTab = "schedule"

        // Update tab styling
        tabMySchedule.setBackgroundResource(R.drawable.bg_input_outline)
        tabMySchedule.setBackgroundTintList(getColorStateList(R.color.white))
        tabMySchedule.setTextColor(getColor(R.color.primary_green))
        tabMySchedule.elevation = 2f

        tabRoomSchedules.setBackgroundResource(R.drawable.bg_input_outline)
        tabRoomSchedules.setBackgroundTintList(getColorStateList(android.R.color.transparent))
        tabRoomSchedules.setTextColor(getColor(R.color.white))
        tabRoomSchedules.elevation = 0f

        // Show/hide containers
        myScheduleContainer.visibility = View.VISIBLE
        roomScheduleContainer.visibility = View.GONE

        // Load data
        loadMySchedule()
    }

    private fun showRoomSchedules() {
        currentTab = "rooms"

        // Update tab styling
        tabRoomSchedules.setBackgroundResource(R.drawable.bg_input_outline)
        tabRoomSchedules.setBackgroundTintList(getColorStateList(R.color.white))
        tabRoomSchedules.setTextColor(getColor(R.color.primary_green))
        tabRoomSchedules.elevation = 2f

        tabMySchedule.setBackgroundResource(R.drawable.bg_input_outline)
        tabMySchedule.setBackgroundTintList(getColorStateList(android.R.color.transparent))
        tabMySchedule.setTextColor(getColor(R.color.white))
        tabMySchedule.elevation = 0f

        // Show/hide containers
        myScheduleContainer.visibility = View.GONE
        roomScheduleContainer.visibility = View.VISIBLE

        // Load data
        loadRoomsFromApi()
    }

    private fun updateViewToggle() {
        if (currentView == "day") {
            btnDay.setBackgroundColor(getColor(R.color.black))
            btnDay.setTextColor(getColor(R.color.white))
            btnWeek.setBackgroundColor(getColor(R.color.white))
            btnWeek.setTextColor(getColor(R.color.text_primary))
            tvListHeader.text = if (currentTab == "schedule") {
                getString(R.string.schedule_for_today)
            } else {
                getString(R.string.schedule_for_today)
            }
        } else {
            btnWeek.setBackgroundColor(getColor(R.color.black))
            btnWeek.setTextColor(getColor(R.color.white))
            btnDay.setBackgroundColor(getColor(R.color.white))
            btnDay.setTextColor(getColor(R.color.text_primary))
            tvListHeader.text = if (currentTab == "schedule") {
                getString(R.string.schedule_for_week)
            } else {
                getString(R.string.schedule_for_week)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateUserName() {
        val prefs = getSharedPreferences("user_session", MODE_PRIVATE)
        val fullName = prefs.getString("full_name", "Teacher") ?: "Teacher"
        tvGreeting.text = "Welcome back,"
        tvProfName.text = fullName
    }

    private fun setupRecyclerViews() {
        rvRooms.layoutManager = LinearLayoutManager(this)
        rvRooms.adapter = roomAdapter

        rvMySchedule.layoutManager = LinearLayoutManager(this)
        rvMySchedule.adapter = scheduleAdapter
    }

    private fun loadMySchedule() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefs = getSharedPreferences("user_session", MODE_PRIVATE)
                val personId = prefs.getInt("person_id", 0)

                if (personId == 0) {
                    Log.e("TeacherDashboard", "Person ID not found")
                    return@launch
                }

                val url = "$BACKEND_URL/get_teacher_schedule.php?teacher_id=$personId"

                val request = Request.Builder()
                    .url(url)
                    .build()

                val response = client.newCall(request).execute()
                val jsonData = response.body?.string() ?: ""

                if (jsonData.isEmpty()) {
                    Log.w("TeacherDashboard", "Empty response from get_teacher_schedule.php")
                    return@launch
                }

                Log.d("TeacherDashboard", "Schedule Response: $jsonData")

                val json = JSONObject(jsonData)
                val success = json.optBoolean("success", false)

                if (success) {
                    val schedulesArray = json.optJSONArray("schedules")
                    val today = json.optString("today", "")

                    if (schedulesArray != null) {
                        val allSchedules = mutableListOf<TeacherScheduleItem>()

                        for (i in 0 until schedulesArray.length()) {
                            try {
                                val obj = schedulesArray.getJSONObject(i)
                                allSchedules.add(
                                    TeacherScheduleItem(
                                        scheduleId = obj.getInt("schedule_ID"),
                                        dayName = obj.getString("day_name"),
                                        timeStart = obj.getString("time_start"),
                                        timeEnd = obj.getString("time_end"),
                                        subjectCode = obj.getString("subject_code"),
                                        subjectName = obj.getString("subject_name"),
                                        sectionName = obj.getString("section_name"),
                                        sectionYear = obj.getInt("section_year"),
                                        roomName = obj.getString("room_name"),
                                        roomCapacity = obj.getInt("room_capacity"),
                                        scheduleStatus = obj.getString("schedule_status"),
                                        isToday = obj.getBoolean("is_today")
                                    )
                                )
                            } catch (e: Exception) {
                                Log.e("TeacherDashboard", "Error parsing schedule at index $i: ${e.message}")
                            }
                        }

                        // Filter based on current view
                        val filteredSchedules = if (currentView == "day") {
                            allSchedules.filter { it.isToday }
                        } else {
                            allSchedules // Show all for week view
                        }

                        withContext(Dispatchers.Main) {
                            scheduleAdapter.submitList(filteredSchedules)
                            Log.d("TeacherDashboard", "✓ Loaded ${filteredSchedules.size} schedules for $currentView view (Today: $today)")
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            scheduleAdapter.submitList(emptyList())
                            Log.w("TeacherDashboard", "No schedules array in response")
                        }
                    }
                } else {
                    val message = json.optString("message", "Failed to load schedules")
                    Log.e("TeacherDashboard", "✗ API error: $message")
                }
            } catch (e: Exception) {
                Log.e("TeacherDashboard", "✗ Error loading schedule: ${e.message}", e)
            }
        }
    }

    private fun loadRoomsFromApi() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val currentDate = dateFormat.format(Date())

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