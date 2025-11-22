package com.example.schedulingSystem

data class ScheduleItem(
    val schedule_ID: String,
    val day_name: String,
    val time_start: String,
    val time_end: String,
    val subject_code: String,
    val subject_name: String,
    val section_name: String,
    val section_year: String,
    val room_name: String,
    val name_first: String,
    val name_last: String
) {
    val teacherFullName: String
        get() = "$name_first $name_last"
}

