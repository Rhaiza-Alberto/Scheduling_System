package com.example.schedulingSystem.models

data class TimeSlot(
    val id: Int,
    val displayName: String,
    val startTime: String,
    val endTime: String
)
