package com.example.schedulingSystem.ui.theme

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.schedulingSystem.R
import com.example.schedulingSystem.adapters.RoomAdapter
import com.example.schedulingSystem.models.Room

class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val sampleRooms = listOf(
            Room("Monday", "08:00 - 10:00", "IT101", "BSIT-1A", "Prof. Cruz", "G301"),
            Room("Wednesday", "10:00 - 12:00", "CS201", "BSCS-2B", "Prof. Reyes", "F204")
        )

        val recycler = findViewById<RecyclerView>(R.id.recyclerRooms)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = RoomAdapter(sampleRooms)
    }
}
