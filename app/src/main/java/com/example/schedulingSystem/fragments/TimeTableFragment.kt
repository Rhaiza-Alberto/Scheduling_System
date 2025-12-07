package com.example.schedulingSystem.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.schedulingSystem.R
import com.example.schedulingSystem.adapters.TimeTableAdapter
import com.example.schedulingSystem.network.ApiResponse
import com.example.schedulingSystem.network.ApiService
import kotlinx.coroutines.launch

class TimetableFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private val timeSlots = listOf(
        "7:00 AM", "7:30 AM", "8:00 AM", "8:30 AM", "9:00 AM", "9:30 AM",
        "10:00 AM", "10:30 AM", "11:00 AM", "11:30 AM", "12:00 PM", "12:30 PM",
        "1:00 PM", "1:30 PM", "2:00 PM", "2:30 PM", "3:00 PM", "3:30 PM",
        "4:00 PM", "4:30 PM", "5:00 PM", "5:30 PM", "6:00 PM", "6:30 PM", "7:00 PM"
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.containerRooms)
        recyclerView.layoutManager = LinearLayoutManager(context)
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
                            .add(TimeTableScheduleItem(s.subject_code, s.section_name, s.teacher_name))
                    }

                    recyclerView.adapter = TimeTableAdapter(timeSlots, map)
                }
                is ApiResponse.Error -> {
                    Toast.makeText(context, "Error: ${response.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun getSchedulesFromApi(): ApiResponse<List<com.example.schedulingSystem.network.ScheduleItemResponse>> {
        val response = ApiService.getAllSchedules()
        return when (response) {
            is ApiResponse.Success -> {
                val schedules = response.data.schedules ?: emptyList()
                ApiResponse.Success(schedules)
            }
            is ApiResponse.Error -> response
        }
    }
}

data class TimeTableScheduleItem(val subject: String, val section: String, val teacher: String)