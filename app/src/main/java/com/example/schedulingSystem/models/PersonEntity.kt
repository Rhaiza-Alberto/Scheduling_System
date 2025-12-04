package com.example.schedulingSystem.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "person")
data class PersonEntity(
    @PrimaryKey
    val personID: Int,
    val personUsername: String,
    val personPassword: String,
    val accountID: Int,
    val nameID: Int,
    val nameFirst: String,
    val nameMiddle: String,
    val nameSecond: String,
    val nameLast: String,
    val nameSuffix: String
)