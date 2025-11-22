package com.example.schedulingSystem.models

data class Room(
    val day: String,
    val time: String,
    val subject: String,
    val section: String,
    val teacher: String,
    val room: String
)