package com.example.schedulingSystem

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.schedulingSystem.utils.SessionManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionManager = SessionManager(this)

        // Verify user is logged in and is admin
        if (!sessionManager.isLoggedIn() || !sessionManager.isAdmin()) {
            redirectToLogin()
            return
        }

        setContentView(R.layout.activity_admin_dashboard)

        setupClickListeners()
        displayUserInfo()
    }

    private fun setupClickListeners() {
        // Settings button
        findViewById<ImageButton>(R.id.btnSettings).setOnClickListener {
            performLogout()
        }

        // Review button
        findViewById<MaterialButton>(R.id.btnReview).setOnClickListener {
            Toast.makeText(this, "Review pending approvals - Coming soon", Toast.LENGTH_SHORT).show()
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
            Toast.makeText(this, "Add new room - Coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun displayUserInfo() {
        val user = sessionManager.getUserData()
        if (user != null) {
            // You can update any TextView to show admin name
            Toast.makeText(this, "Welcome, ${user.name}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun performLogout() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                sessionManager.clearSession()
                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
                redirectToLogin()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun redirectToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}