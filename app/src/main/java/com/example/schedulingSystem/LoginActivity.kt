package com.example.schedulingSystem

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnSignIn: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize views
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnSignIn = findViewById(R.id.btnSignIn)

        // Set up click listener for sign in button
        btnSignIn.setOnClickListener {
            handleLogin()
        }

        // Optional: Forgot password click listener
        findViewById<android.widget.TextView>(R.id.tvForgotPassword)?.setOnClickListener {
            Toast.makeText(this, "Password reset feature coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleLogin() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        // Validate inputs
        if (email.isEmpty()) {
            etEmail.error = "Email is required"
            etEmail.requestFocus()
            return
        }

        if (password.isEmpty()) {
            etPassword.error = "Password is required"
            etPassword.requestFocus()
            return
        }

        // Simple hardcoded authentication for testing
        when {
            // Admin login
            email == "admin@wmsu.edu.ph" && password == "admin123" -> {
                Toast.makeText(this, "Welcome Admin!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, AdminDashboardActivity::class.java))
                finish()
            }
            // Professor login
            email == "professor@wmsu.edu.ph" && password == "prof123" -> {
                Toast.makeText(this, "Welcome Professor!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, ProfessorDashboardActivity::class.java))
                finish()
            }
            // Invalid credentials
            else -> {
                Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
            }
        }
    }
}