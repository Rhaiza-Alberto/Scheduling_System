package com.example.schedulingSystem.ui.theme

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.schedulingSystem.network.ApiResponse
import com.example.schedulingSystem.network.ApiService
import com.example.schedulingSystem.utils.SessionManager
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProfessorDashboardActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvProfName: TextView
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_professor_dashboard)

        sessionManager = SessionManager(this)

        // Verify user is logged in and is a teacher
        if (!sessionManager.isLoggedIn() || !sessionManager.isTeacher()) {
            redirectToLogin()
            return
        }

        initViews()
        setupUserInfo()
        setupNavigation()
        loadSchedules()
    }

    private fun initViews() {
        drawerLayout = findViewById(R.id.drawerLayout)
        navView = findViewById(R.id.navView)
        recyclerView = findViewById(R.id.rvRooms)
        tvProfName = findViewById(R.id.tvProfName)

        recyclerView.layoutManager = LinearLayoutManager(this)

        findViewById<android.widget.ImageView>(R.id.btnOpenDrawer).setOnClickListener {
            drawerLayout.openDrawer(navView)
        }
    }

    private fun setupUserInfo() {
        val user = sessionManager.getUserData()
        if (user != null) {
            tvProfName.text = user.name

            // Update navigation header
            val headerView = navView.getHeaderView(0)
            headerView.findViewById<TextView>(R.id.imgNavProfile)?.apply {
                // You can set profile picture here
            }
        }
    }

    private fun setupNavigation() {
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    // Already on home
                    drawerLayout.closeDrawers()
                }
                R.id.nav_my_requests -> {
                    Toast.makeText(this, "My Requests - Coming soon", Toast.LENGTH_SHORT).show()
                    drawerLayout.closeDrawers()
                }
                R.id.nav_profile -> {
                    Toast.makeText(this, "Profile - Coming soon", Toast.LENGTH_SHORT).show()
                    drawerLayout.closeDrawers()
                }
                R.id.nav_logout -> {
                    performLogout()
                }
            }
            true
        }
    }

    private fun loadSchedules() {
        CoroutineScope(Dispatchers.Main).launch {
            when (val response = ApiService.getTodaySchedule()) {
                is ApiResponse.Success -> {
                    val schedules = response.data
                    recyclerView.adapter = ScheduleAdapter(schedules)

                    if (schedules.isEmpty()) {
                        Toast.makeText(
                            this@ProfessorDashboardActivity,
                            "No schedules for today",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                is ApiResponse.Error -> {
                    Toast.makeText(
                        this@ProfessorDashboardActivity,
                        "Failed to load schedules: ${response.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun performLogout() {
        sessionManager.clearSession()
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
        redirectToLogin()
    }

    private fun redirectToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}