package com.example.schedulingSystem.models

data class User(
    val personId: Int,
    val email: String,
    val accountType: String,
    val fullName: String,
    val firstName: String = "",
    val middleName: String = "",
    val lastName: String = "",
    val suffix: String = ""
)