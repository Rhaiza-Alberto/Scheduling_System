package com.example.schedulingSystem.models

data class UserItem(
    val userId: Int,
    val fullName: String,
    val email: String,
    val accountType: String,
    val status: String
)