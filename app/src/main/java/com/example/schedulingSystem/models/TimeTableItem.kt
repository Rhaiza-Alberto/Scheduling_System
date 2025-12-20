package com.example.schedulingSystem.models

sealed class TimetableItem {
    data class Header(val text: String) : TimetableItem()
    data class TimeLabel(val time: String) : TimetableItem()
    data object Empty : TimetableItem()
    data class ClassBlock(
        val subject: String,
        val section: String,
        val teacher: String,
        val rowSpan: Int = 1,
        val status: String = "Pending" // "Pending", "Occupied", or empty
    ) : TimetableItem()
}