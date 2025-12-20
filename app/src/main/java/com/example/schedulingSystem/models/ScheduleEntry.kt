package com.example.schedulingSystem.models

data class ScheduleEntry(
    val dayName: String,
    val startDisplay: String,
    val endDisplay: String,
    val subject: String,
    val section: String,
    val teacher: String,
    val status: String = "Pending" // Default to "Pending", can be "Pending", "Occupied", or empty for no schedule
)