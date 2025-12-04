package com.example.schedulingSystem

import com.google.gson.annotations.SerializedName

data class ScheduleItem(
    @SerializedName("schedule_ID")      val scheduleID: String,
    @SerializedName("day_name")         val dayName: String,
    @SerializedName("time_start")       val timeStart: String,
    @SerializedName("time_end")         val timeEnd: String,
    @SerializedName("subject_code")     val subjectCode: String?,
    @SerializedName("subject_name")     val subjectName: String?,
    @SerializedName("section_name")    val sectionName: String?,
    @SerializedName("section_year")    val sectionYear: String?,
    @SerializedName("room_name")        val roomName: String?,
    @SerializedName("name_first")       val nameFirst: String?,
    @SerializedName("name_last")        val nameLast: String?,
    @SerializedName("schedule_status")  val scheduleStatus: String?
) {
    val teacherFullName: String
        get() = "$nameFirst $nameLast".trim().ifBlank { "Unknown Teacher" }
}