package com.example.schedulingSystem.models

data class TimeSlotData(
    val timeSlot: String,
    val daySchedules: Map<String, TimeSlotContent>
)

data class TimeSlotContent(
    val subject: String,
    val section: String,
    val teacher: String?,
    val isFree: Boolean = false
)
