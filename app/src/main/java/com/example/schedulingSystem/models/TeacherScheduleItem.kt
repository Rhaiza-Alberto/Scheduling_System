package com.example.schedulingSystem.models

import com.google.gson.annotations.SerializedName

data class TeacherScheduleItem(
    @SerializedName("schedule_ID")
    val scheduleId: Int,

    @SerializedName("day_name")
    val dayName: String,

    @SerializedName("time_start")
    val timeStart: String,

    @SerializedName("time_end")
    val timeEnd: String,

    @SerializedName("subject_code")
    val subjectCode: String,

    @SerializedName("subject_name")
    val subjectName: String,

    @SerializedName("section_name")
    val sectionName: String,

    @SerializedName("section_year")
    val sectionYear: Int,

    @SerializedName("room_name")
    val roomName: String,

    @SerializedName("room_capacity")
    val roomCapacity: Int,

    @SerializedName("schedule_status")
    val scheduleStatus: String,

    @SerializedName("is_today")
    val isToday: Boolean = false
)