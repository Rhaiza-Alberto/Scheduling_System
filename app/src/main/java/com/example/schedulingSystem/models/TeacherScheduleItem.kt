package com.example.schedulingSystem.models

data class TeacherScheduleItem(
    val scheduleId: Int,
    val dayName: String,
    val timeStart: String,
    val timeEnd: String,
    val subjectCode: String,
    val subjectName: String,
    val sectionName: String,
    val sectionYear: Int,
    val roomName: String,
    val roomCapacity: Int,
    val scheduleStatus: String,
    val isToday: Boolean
)