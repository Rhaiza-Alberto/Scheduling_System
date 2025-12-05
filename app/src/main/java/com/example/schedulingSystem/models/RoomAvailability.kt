package com.example.schedulingSystem.models

data class RoomAvailability(
    val roomId: Int,
    val roomName: String,
    val roomCapacity: Int,
    val status: String,
    val isAvailable: Boolean
)