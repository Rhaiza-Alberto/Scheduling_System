package com.example.schedulingSystem

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.schedulingSystem.adapters.TimeTableAdapter
import com.example.schedulingSystem.fragments.TimeTableScheduleItem
import com.example.schedulingSystem.network.ApiResponse
import com.example.schedulingSystem.network.ApiService
import com.example.schedulingSystem.network.ScheduleItemResponse
import kotlinx.coroutines.launch

class AdminDashboardScheduleRoom : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnSettings: ImageButton
    private val timeSlots = listOf(
        "7:00 AM", "7:30 AM", "8:00 AM", "8:30 AM", "9:00 AM", "9:30 AM",
        "10:00 AM", "10:30 AM", "11:00 AM", "11:30 AM", "12:00 PM", "12:30 PM",
        "1:00 PM", "1:30 PM", "2:00 PM", "2:30 PM", "3:00 PM", "3:30 PM",
        "4:00 PM", "4:30 PM", "5:00 PM", "5:30 PM", "6:00 PM", "6:30 PM", "7:00 PM"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_room_schedule)

        // Get room info from intent
        val roomId = intent.getIntExtra("room_id", -1)
        val roomName = intent.getStringExtra("room_name") ?: "Room"

        // Initialize views
        recyclerView = findViewById(R.id.containerRooms)
        btnSettings = findViewById(R.id.btnSettings)

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Setup settings button for logout
        btnSettings.setOnClickListener {
            logout()
        }

        // Load schedule data for this room
        loadSchedule()
    }

    private fun loadSchedule() {
        lifecycleScope.launch {
            when (val response = getSchedulesFromApi()) {
                is ApiResponse.Success -> {
                    val schedules = response.data
                    val map = mutableMapOf<String, MutableList<TimeTableScheduleItem>>()

                    for (s in schedules) {
                        val key = "${s.day_name}_${s.time_start}"
                        map.getOrPut(key) { mutableListOf() }
                            .add(TimeTableScheduleItem(
                                subject = s.subject_name ?: s.subject_code ?: "Unknown",
                                section = s.section_name ?: "Unknown",
                                teacher = s.teacher_name
                            ))
                    }

                    recyclerView.adapter = TimeTableAdapter(timeSlots, map)
                }
                is ApiResponse.Error -> {
                    Toast.makeText(this@AdminDashboardScheduleRoom, "Error: ${response.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun getSchedulesFromApi(): ApiResponse<List<ScheduleItemResponse>> {
        val response = ApiService.getAllSchedules()
        return when (response) {
            is ApiResponse.Success -> {
                val schedules = response.data.schedules ?: emptyList()
                ApiResponse.Success(schedules)
            }
            is ApiResponse.Error -> response
        }
    }

    private fun logout() {
        // Clear session
        val sharedPref = getSharedPreferences("user_session", MODE_PRIVATE)
        sharedPref.edit().clear().apply()

        // Redirect to login
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}