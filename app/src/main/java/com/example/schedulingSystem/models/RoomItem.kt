package com.example.schedulingSystem.models

/**
 * Data class for room availability in Teacher Dashboard
 */
data class RoomItem(
    val roomId: Int,
    val roomName: String,
    val roomCapacity: Int,
    val status: String,
    val isAvailable: Boolean
)