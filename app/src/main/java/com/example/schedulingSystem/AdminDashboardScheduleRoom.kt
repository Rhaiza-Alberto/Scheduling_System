package com.example.schedulingSystem

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.schedulingSystem.adapters.TimeSlotAdapter
import com.example.schedulingSystem.network.ApiResponse
import com.example.schedulingSystem.network.ApiService
import com.example.schedulingSystem.network.ScheduleItemResponse
import kotlinx.coroutines.launch
import com.example.schedulingSystem.models.TimeSlotData
import com.example.schedulingSystem.models.TimeSlotContent


class AdminDashboardScheduleRoom : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnSettings: ImageButton
    
    // All 30-minute time slots
    private val allTimeSlots = listOf(
        "7:00", "7:30", "8:00", "8:30", "9:00", "9:30",
        "10:00", "10:30", "11:00", "11:30", "12:00", "12:30",
        "1:00", "1:30", "2:00", "2:30", "3:00", "3:30",
        "4:00", "4:30", "5:00", "5:30", "6:00", "6:30", "7:00"
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
                    val timeSlotDataList = buildTimeSlotGrid(schedules)
                    recyclerView.adapter = TimeSlotAdapter(timeSlotDataList)
                }
                is ApiResponse.Error -> {
                    Toast.makeText(this@AdminDashboardScheduleRoom, "Error: ${response.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun buildTimeSlotGrid(schedules: List<ScheduleItemResponse>): List<TimeSlotData> {
        val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        val result = mutableListOf<TimeSlotData>()

        // For each time slot
        for (timeSlot in allTimeSlots) {
            val daySchedules = mutableMapOf<String, TimeSlotContent>()

            // For each day
            for (day in days) {
                // Find if there's a schedule that covers this time slot
                val matchingSchedule = schedules.find { schedule ->
                    schedule.day_name == day && isTimeSlotInRange(
                        timeSlot,
                        schedule.raw_start_time ?: schedule.time_start,
                        schedule.raw_end_time ?: schedule.time_end
                    )
                }

                daySchedules[day] = if (matchingSchedule != null) {
                    TimeSlotContent(
                        subject = matchingSchedule.subject_name ?: matchingSchedule.subject_code ?: "Unknown",
                        section = matchingSchedule.section_name ?: "Unknown",
                        teacher = matchingSchedule.teacher_name,
                        isFree = false
                    )
                } else {
                    TimeSlotContent(
                        subject = "Free",
                        section = "",
                        teacher = null,
                        isFree = true
                    )
                }
            }

            result.add(TimeSlotData(timeSlot, daySchedules))
        }

        return result
    }

    private fun isTimeSlotInRange(timeSlot: String, startTime: String, endTime: String): Boolean {
        return try {
            val slotMinutes = timeToMinutes(timeSlot)
            val startMinutes = timeToMinutes(startTime)
            val endMinutes = timeToMinutes(endTime)
            slotMinutes >= startMinutes && slotMinutes < endMinutes
        } catch (e: Exception) {
            false
        }
    }

    private fun timeToMinutes(time: String): Int {
        return try {
            // Handle both "7:00" and "7:00 AM" formats
            val cleanTime = time.trim().replace(" AM", "").replace(" PM", "").replace(" am", "").replace(" pm", "")
            val parts = cleanTime.split(":")
            val hours = parts[0].toInt()
            val minutes = if (parts.size > 1) parts[1].toInt() else 0
            hours * 60 + minutes
        } catch (e: Exception) {
            0
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