package com.example.schedulingSystem

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.schedulingSystem.models.TeacherScheduleItem
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.*

class TeacherDashboardActivity : AppCompatActivity() {

    private lateinit var rvSchedule: RecyclerView
    private lateinit var tvProfName: TextView
    private lateinit var tvGreeting: TextView
    private lateinit var btnSettings: ImageView
    private lateinit var tvListHeader: TextView
    private lateinit var btnDay: MaterialButton
    private lateinit var btnWeek: MaterialButton

    private val scheduleAdapter = TeacherScheduleAdapter()
    private var currentView = "day" // "day" or "week"

    // Store all mock data
    private val allSchedules = mutableListOf<TeacherScheduleItem>()

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
        generateMockData()
        filterAndDisplaySchedule()
    }

    private fun initViews() {
        rvSchedule = findViewById(R.id.rvSchedule)
        tvProfName = findViewById(R.id.tvProfName)
        tvGreeting = findViewById(R.id.tvGreeting)
        btnSettings = findViewById(R.id.btnSettings)
        tvListHeader = findViewById(R.id.tvListHeader)
        btnDay = findViewById(R.id.btnDay)
        btnWeek = findViewById(R.id.btnWeek)
    }

    private fun setupClickListeners() {
        btnSettings.setOnClickListener {
            performLogout()
        }

        // Day View Button
        btnDay.setOnClickListener {
            currentView = "day"
            updateViewToggleButtons()
            filterAndDisplaySchedule()
        }

        // Week View Button
        btnWeek.setOnClickListener {
            currentView = "week"
            updateViewToggleButtons()
            filterAndDisplaySchedule()
        }

        // Room Schedules Tab (disabled/placeholder)
        findViewById<TextView>(R.id.tabRoomSchedules).setOnClickListener {
            Toast.makeText(this, "Room Schedules - Coming Soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateViewToggleButtons() {
        if (currentView == "day") {
            // Day button active
            btnDay.apply {
                backgroundTintList = ContextCompat.getColorStateList(this@TeacherDashboardActivity, R.color.primary_green)
                setTextColor(ContextCompat.getColor(this@TeacherDashboardActivity, R.color.white))
                strokeWidth = 0
            }
            // Week button inactive
            btnWeek.apply {
                backgroundTintList = ContextCompat.getColorStateList(this@TeacherDashboardActivity, R.color.white)
                setTextColor(ContextCompat.getColor(this@TeacherDashboardActivity, R.color.primary_green))
                strokeWidth = 4
            }
        } else {
            // Week button active
            btnWeek.apply {
                backgroundTintList = ContextCompat.getColorStateList(this@TeacherDashboardActivity, R.color.primary_green)
                setTextColor(ContextCompat.getColor(this@TeacherDashboardActivity, R.color.white))
                strokeWidth = 0
            }
            // Day button inactive
            btnDay.apply {
                backgroundTintList = ContextCompat.getColorStateList(this@TeacherDashboardActivity, R.color.white)
                setTextColor(ContextCompat.getColor(this@TeacherDashboardActivity, R.color.primary_green))
                strokeWidth = 4
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

    private fun setupRecyclerView() {
        rvSchedule.layoutManager = LinearLayoutManager(this)
        rvSchedule.adapter = scheduleAdapter
    }

    private fun generateMockData() {
        // Get current day
        val dateFormat = SimpleDateFormat("EEEE", Locale.getDefault())
        val currentDay = dateFormat.format(Date())

        // Define week days
        val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")

        // Mock schedule data
        val mockSchedules = listOf(
            // Monday
            TeacherScheduleItem(
                scheduleId = 1,
                dayName = "Monday",
                timeStart = "08:00 AM",
                timeEnd = "09:30 AM",
                subjectCode = "CC 101",
                subjectName = "Computer Programming",
                sectionName = "IT 1A",
                sectionYear = 1,
                roomName = "Lab 101",
                roomCapacity = 40,
                scheduleStatus = "Completed",
                isToday = currentDay == "Monday"
            ),
            TeacherScheduleItem(
                scheduleId = 2,
                dayName = "Monday",
                timeStart = "10:00 AM",
                timeEnd = "11:30 AM",
                subjectCode = "IT 201",
                subjectName = "Data Structures",
                sectionName = "IT 2B",
                sectionYear = 2,
                roomName = "Lab 102",
                roomCapacity = 35,
                scheduleStatus = "Completed",
                isToday = currentDay == "Monday"
            ),
            TeacherScheduleItem(
                scheduleId = 3,
                dayName = "Monday",
                timeStart = "01:00 PM",
                timeEnd = "02:30 PM",
                subjectCode = "CS 301",
                subjectName = "Algorithm Analysis",
                sectionName = "CS 3A",
                sectionYear = 3,
                roomName = "Room 204",
                roomCapacity = 45,
                scheduleStatus = "Completed",
                isToday = currentDay == "Monday"
            ),

            // Tuesday
            TeacherScheduleItem(
                scheduleId = 4,
                dayName = "Tuesday",
                timeStart = "08:00 AM",
                timeEnd = "09:30 AM",
                subjectCode = "IT 201",
                subjectName = "Data Structures",
                sectionName = "IT 2A",
                sectionYear = 2,
                roomName = "Lab 103",
                roomCapacity = 40,
                scheduleStatus = "Completed",
                isToday = currentDay == "Tuesday"
            ),
            TeacherScheduleItem(
                scheduleId = 5,
                dayName = "Tuesday",
                timeStart = "11:00 AM",
                timeEnd = "12:30 PM",
                subjectCode = "CC 101",
                subjectName = "Computer Programming",
                sectionName = "IT 1B",
                sectionYear = 1,
                roomName = "Lab 101",
                roomCapacity = 40,
                scheduleStatus = "Completed",
                isToday = currentDay == "Tuesday"
            ),
            TeacherScheduleItem(
                scheduleId = 6,
                dayName = "Tuesday",
                timeStart = "02:00 PM",
                timeEnd = "03:30 PM",
                subjectCode = "CS 401",
                subjectName = "Software Engineering",
                sectionName = "CS 4A",
                sectionYear = 4,
                roomName = "Room 305",
                roomCapacity = 50,
                scheduleStatus = "Completed",
                isToday = currentDay == "Tuesday"
            ),

            // Wednesday
            TeacherScheduleItem(
                scheduleId = 7,
                dayName = "Wednesday",
                timeStart = "09:00 AM",
                timeEnd = "10:30 AM",
                subjectCode = "CS 301",
                subjectName = "Algorithm Analysis",
                sectionName = "CS 3B",
                sectionYear = 3,
                roomName = "Room 205",
                roomCapacity = 42,
                scheduleStatus = "Completed",
                isToday = currentDay == "Wednesday"
            ),
            TeacherScheduleItem(
                scheduleId = 8,
                dayName = "Wednesday",
                timeStart = "01:00 PM",
                timeEnd = "02:30 PM",
                subjectCode = "IT 301",
                subjectName = "Web Development",
                sectionName = "IT 3A",
                sectionYear = 3,
                roomName = "Lab 104",
                roomCapacity = 38,
                scheduleStatus = "Completed",
                isToday = currentDay == "Wednesday"
            ),

            // Thursday
            TeacherScheduleItem(
                scheduleId = 9,
                dayName = "Thursday",
                timeStart = "08:00 AM",
                timeEnd = "09:30 AM",
                subjectCode = "CC 101",
                subjectName = "Computer Programming",
                sectionName = "IT 1C",
                sectionYear = 1,
                roomName = "Lab 102",
                roomCapacity = 35,
                scheduleStatus = "Completed",
                isToday = currentDay == "Thursday"
            ),
            TeacherScheduleItem(
                scheduleId = 10,
                dayName = "Thursday",
                timeStart = "10:00 AM",
                timeEnd = "11:30 AM",
                subjectCode = "IT 301",
                subjectName = "Web Development",
                sectionName = "IT 3B",
                sectionYear = 3,
                roomName = "Lab 103",
                roomCapacity = 40,
                scheduleStatus = "Completed",
                isToday = currentDay == "Thursday"
            ),
            TeacherScheduleItem(
                scheduleId = 11,
                dayName = "Thursday",
                timeStart = "02:00 PM",
                timeEnd = "03:30 PM",
                subjectCode = "CS 401",
                subjectName = "Software Engineering",
                sectionName = "CS 4B",
                sectionYear = 4,
                roomName = "Room 306",
                roomCapacity = 48,
                scheduleStatus = "Completed",
                isToday = currentDay == "Thursday"
            ),

            // Friday
            TeacherScheduleItem(
                scheduleId = 12,
                dayName = "Friday",
                timeStart = "09:00 AM",
                timeEnd = "10:30 AM",
                subjectCode = "IT 201",
                subjectName = "Data Structures",
                sectionName = "IT 2C",
                sectionYear = 2,
                roomName = "Lab 101",
                roomCapacity = 40,
                scheduleStatus = "Completed",
                isToday = currentDay == "Friday"
            ),
            TeacherScheduleItem(
                scheduleId = 13,
                dayName = "Friday",
                timeStart = "11:00 AM",
                timeEnd = "12:30 PM",
                subjectCode = "CS 301",
                subjectName = "Algorithm Analysis",
                sectionName = "CS 3C",
                sectionYear = 3,
                roomName = "Room 206",
                roomCapacity = 44,
                scheduleStatus = "Completed",
                isToday = currentDay == "Friday"
            ),
            TeacherScheduleItem(
                scheduleId = 14,
                dayName = "Friday",
                timeStart = "01:30 PM",
                timeEnd = "03:00 PM",
                subjectCode = "IT 401",
                subjectName = "Capstone Project",
                sectionName = "IT 4A",
                sectionYear = 4,
                roomName = "Lab 105",
                roomCapacity = 30,
                scheduleStatus = "Completed",
                isToday = currentDay == "Friday"
            )
        )

        allSchedules.clear()
        allSchedules.addAll(mockSchedules)

        Log.d("TeacherDashboard", "Generated ${allSchedules.size} mock schedules. Current day: $currentDay")
    }

    private fun filterAndDisplaySchedule() {
        val filteredList = if (currentView == "day") {
            // Show only today's schedule
            allSchedules.filter { it.isToday }
        } else {
            // Show full week schedule
            allSchedules
        }

        scheduleAdapter.submitList(filteredList)

        Log.d("TeacherDashboard", "Displaying ${filteredList.size} schedules in $currentView view")

        if (filteredList.isEmpty() && currentView == "day") {
            Toast.makeText(this, "No classes scheduled for today", Toast.LENGTH_SHORT).show()
        }
    }

    private fun performLogout() {
        AlertDialog.Builder(this)
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