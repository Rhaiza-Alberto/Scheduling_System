package com.example.schedulingSystem

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.schedulingSystem.models.TeacherScheduleItem
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class TeacherDashboardActivity : AppCompatActivity() {

    private lateinit var rvSchedule: RecyclerView
    private lateinit var tvProfName: TextView
    private lateinit var tvGreeting: TextView
    private lateinit var btnSettings: ImageView
    private lateinit var tvListHeader: TextView
    private lateinit var tvRequestCount: TextView

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val scheduleAdapter = TeacherScheduleAdapter()
    private val gson = Gson()

    companion object {
        private const val BACKEND_URL = "http://10.0.2.2/scheduling-api"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if user is logged in and is teacher
        val prefs = getSharedPreferences("user_session", MODE_PRIVATE)
        val isLoggedIn = prefs.getBoolean("is_logged_in", false)
        val accountType = prefs.getString("account_type", "")
        val personId = prefs.getInt("person_id", -1)

        if (!isLoggedIn) {
            Log.w("TeacherDashboard", "User not logged in, redirecting to login")
            redirectToLogin()
            return
        }

        if (accountType?.lowercase() != "teacher") {
            Log.w("TeacherDashboard", "User is not teacher (type: $accountType), redirecting to login")
            redirectToLogin()
            return
        }

        if (personId == -1) {
            Log.e("TeacherDashboard", "Person ID not found in session")
            Toast.makeText(this, "Person ID not found. Please login again.", Toast.LENGTH_LONG).show()
            redirectToLogin()
            return
        }

        setContentView(R.layout.activity_teacher_dashboard)

        initViews()
        setupClickListeners()
        updateUserName()
        setupRecyclerView()
        loadTeacherSchedule(personId)
    }

    private fun initViews() {
        rvSchedule = findViewById(R.id.rvRooms)
        tvProfName = findViewById(R.id.tvProfName)
        tvGreeting = findViewById(R.id.tvGreeting)
        btnSettings = findViewById(R.id.btnSettings)
        tvListHeader = findViewById(R.id.tvListHeader)
        tvRequestCount = findViewById(R.id.tvRequestCount)
    }

    private fun setupClickListeners() {
        btnSettings.setOnClickListener {
            performLogout()
        }

        findViewById<MaterialButton>(R.id.btnViewAll).setOnClickListener {
            Toast.makeText(this, "View All Requests - Coming Soon", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateUserName() {
        val prefs = getSharedPreferences("user_session", MODE_PRIVATE)
        val fullName = prefs.getString("full_name", "Teacher") ?: "Teacher"
        tvGreeting.text = "Welcome back,"
        tvProfName.text = fullName
    }

    private fun setupRecyclerView() {
        rvSchedule.layoutManager = LinearLayoutManager(this)
        rvSchedule.adapter = scheduleAdapter
    }

    private fun loadTeacherSchedule(personId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("TeacherDashboard", "Fetching schedule for person ID: $personId")

                val request = Request.Builder()
                    .url("$BACKEND_URL/get_teacher_schedule.php?person_id=$personId")
                    .build()

                val response = client.newCall(request).execute()
                val jsonData = response.body?.string() ?: ""

                Log.d("TeacherDashboard", "Response received: $jsonData")

                if (jsonData.isEmpty()) {
                    Log.w("TeacherDashboard", "Empty response from server")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@TeacherDashboardActivity,
                            "No response from server",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    return@launch
                }

                val json = JSONObject(jsonData)
                val success = json.optBoolean("success", false)

                if (success) {
                    // Parse using Gson for better type safety
                    val schedulesJson = json.getJSONArray("schedules").toString()
                    val scheduleType = object : TypeToken<List<TeacherScheduleItem>>() {}.type
                    val scheduleList: List<TeacherScheduleItem> = gson.fromJson(schedulesJson, scheduleType)

                    val today = json.optString("today", "")

                    // Count today's classes
                    val todayCount = scheduleList.count { it.isToday }

                    withContext(Dispatchers.Main) {
                        scheduleAdapter.submitList(scheduleList)
                        tvListHeader.text = "Your Schedule"
                        tvRequestCount.text = if (todayCount > 0) {
                            "$todayCount ${if (todayCount == 1) "class" else "classes"} today"
                        } else {
                            "No classes today"
                        }

                        Log.d("TeacherDashboard", "✓ Loaded ${scheduleList.size} schedules ($todayCount today)")

                        if (scheduleList.isEmpty()) {
                            Toast.makeText(
                                this@TeacherDashboardActivity,
                                "No schedules found for this teacher",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            Toast.makeText(
                                this@TeacherDashboardActivity,
                                "Loaded ${scheduleList.size} schedules",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    val message = json.optString("message", "Failed to load schedule")
                    Log.e("TeacherDashboard", "✗ API error: $message")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@TeacherDashboardActivity,
                            "Error: $message",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("TeacherDashboard", "✗ Error loading schedule: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@TeacherDashboardActivity,
                        "Network error: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun performLogout() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                getSharedPreferences("user_session", MODE_PRIVATE).edit { clear() }
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