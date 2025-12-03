package com.example.schedulingSystem

import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton

class AdminDashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Settings button
        findViewById<ImageButton>(R.id.btnSettings).setOnClickListener {
            Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show()
        }

        // Review button
        findViewById<MaterialButton>(R.id.btnReview).setOnClickListener {
            Toast.makeText(this, "Review pending approvals", Toast.LENGTH_SHORT).show()
        }

        // Room 1 actions
        findViewById<ImageButton>(R.id.btnEdit1).setOnClickListener {
            Toast.makeText(this, "Edit Lab 101", Toast.LENGTH_SHORT).show()
        }

        findViewById<ImageButton>(R.id.btnDelete1).setOnClickListener {
            Toast.makeText(this, "Delete Lab 101", Toast.LENGTH_SHORT).show()
        }

        // Room 2 actions
        findViewById<ImageButton>(R.id.btnEdit2).setOnClickListener {
            Toast.makeText(this, "Edit Lecture Hall A", Toast.LENGTH_SHORT).show()
        }

        findViewById<ImageButton>(R.id.btnDelete2).setOnClickListener {
            Toast.makeText(this, "Delete Lecture Hall A", Toast.LENGTH_SHORT).show()
        }

        // FAB - Add room
        findViewById<FloatingActionButton>(R.id.fabAddRoom).setOnClickListener {
            Toast.makeText(this, "Add new room", Toast.LENGTH_SHORT).show()
        }
    }
}