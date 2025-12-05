package com.example.schedulingSystem

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import androidx.core.content.edit
import java.util.concurrent.TimeUnit
import android.view.View
import android.widget.LinearLayout

class AdminDashboardActivity : AppCompatActivity() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val BACKEND_URL = "http://10.0.2.2/scheduling-api"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if user is logged in and is admin
        val prefs = getSharedPreferences("user_session", MODE_PRIVATE)
        val isLoggedIn = prefs.getBoolean("is_logged_in", false)
        val accountType = prefs.getString("account_type", "")

        if (!isLoggedIn) {
            Log.w("AdminDashboard", "User not logged in, redirecting to login")
            redirectToLogin()
            return
        }

        if (accountType?.lowercase() != "admin") {
            Log.w("AdminDashboard", "User is not admin (type: $accountType), redirecting to login")
            Toast.makeText(this, "Access denied: Admin only", Toast.LENGTH_LONG).show()
            redirectToLogin()
            return
        }

        setContentView(R.layout.activity_admin_dashboard)

        setupClickListeners()
        displayUserInfo()
        loadDashboardData()
    }

    private var isFabMenuOpen = false

    private fun setupClickListeners() {
        // Settings button (logout)
        findViewById<ImageButton>(R.id.btnSettings).setOnClickListener {
            performLogout()
        }

        // Review button
        findViewById<MaterialButton>(R.id.btnReview).setOnClickListener {
            Toast.makeText(this, "Review pending approvals - Coming soon", Toast.LENGTH_SHORT).show()
        }

        // Main FAB - Toggle menu
        val fabMain = findViewById<FloatingActionButton>(R.id.fabMain)
        val layoutAddUser = findViewById<LinearLayout>(R.id.layoutAddUser)
        val layoutAddRoom = findViewById<LinearLayout>(R.id.layoutAddRoom)
        val layoutAddSchedule = findViewById<LinearLayout>(R.id.layoutAddSchedule)
        val fabOverlay = findViewById<View>(R.id.fabOverlay)

        val fabAddUser = findViewById<FloatingActionButton>(R.id.fabAddUser)
        val fabAddRoom = findViewById<FloatingActionButton>(R.id.fabAddRoomOption)
        val fabAddSchedule = findViewById<FloatingActionButton>(R.id.fabAddSchedule)

        fabMain.setOnClickListener {
            if (isFabMenuOpen) {
                closeFabMenu(layoutAddUser, layoutAddRoom, layoutAddSchedule, fabOverlay, fabMain)
            } else {
                openFabMenu(layoutAddUser, layoutAddRoom, layoutAddSchedule, fabOverlay, fabMain)
            }
        }

        // Overlay click = close menu
        fabOverlay.setOnClickListener {
            closeFabMenu(layoutAddUser, layoutAddRoom, layoutAddSchedule, fabOverlay, fabMain)
        }

        // Individual FAB actions
        fabAddUser.setOnClickListener {
            Toast.makeText(this, "Add User - Coming soon", Toast.LENGTH_SHORT).show()
            closeFabMenu(layoutAddUser, layoutAddRoom, layoutAddSchedule, fabOverlay, fabMain)
        }

        fabAddRoom.setOnClickListener {
            Toast.makeText(this, "Add Room - Coming soon", Toast.LENGTH_SHORT).show()
            closeFabMenu(layoutAddUser, layoutAddRoom, layoutAddSchedule, fabOverlay, fabMain)
        }

        fabAddSchedule.setOnClickListener {
            Toast.makeText(this, "Add Schedule - Coming soon", Toast.LENGTH_SHORT).show()
            closeFabMenu(layoutAddUser, layoutAddRoom, layoutAddSchedule, fabOverlay, fabMain)
        }
    }

    private fun openFabMenu(
        layoutAddUser: LinearLayout,
        layoutAddRoom: LinearLayout,
        layoutAddSchedule: LinearLayout,
        overlay: View,
        fabMain: FloatingActionButton ) {
        isFabMenuOpen = true

        overlay.visibility = View.VISIBLE
        layoutAddSchedule.visibility = View.VISIBLE
        layoutAddRoom.visibility = View.VISIBLE
        layoutAddUser.visibility = View.VISIBLE

        // Rotate main FAB to "X"
        fabMain.rotation = 0f
        fabMain.animate().rotation(135f).setDuration(300).start()

        // Animate options in
        layoutAddSchedule.translationY = 200f
        layoutAddRoom.translationY = 200f
        layoutAddUser.translationY = 200f

        layoutAddSchedule.alpha = 0f
        layoutAddRoom.alpha = 0f
        layoutAddUser.alpha = 0f

        layoutAddSchedule.animate().translationY(0f).alpha(1f).setDuration(300).setStartDelay(50).start()
        layoutAddRoom.animate().translationY(0f).alpha(1f).setDuration(300).setStartDelay(100).start()
        layoutAddUser.animate().translationY(0f).alpha(1f).setDuration(300).setStartDelay(150).start()

        overlay.animate().alpha(0.6f).setDuration(300).start()
    }

    private fun closeFabMenu(
        layoutAddUser: LinearLayout,
        layoutAddRoom: LinearLayout,
        layoutAddSchedule: LinearLayout,
        overlay: View,
        fabMain: FloatingActionButton ) {
        isFabMenuOpen = false

        fabMain.animate().rotation(0f).setDuration(300).start()

        layoutAddSchedule.animate()
            .translationY(200f)
            .alpha(0f)
            .setDuration(250)
            .withEndAction { layoutAddSchedule.visibility = View.GONE }
            .start()

        layoutAddRoom.animate()
            .translationY(200f)
            .alpha(0f)
            .setDuration(250)
            .withEndAction { layoutAddRoom.visibility = View.GONE }
            .start()

        layoutAddUser.animate()
            .translationY(200f)
            .alpha(0f)
            .setDuration(250)
            .withEndAction { layoutAddUser.visibility = View.GONE }
            .start()

        overlay.animate().alpha(0f).setDuration(300).withEndAction {
            overlay.visibility = View.GONE
        }.start()
    }

    private fun displayUserInfo() {
        val prefs = getSharedPreferences("user_session", MODE_PRIVATE)
        val fullName = prefs.getString("full_name", "Admin") ?: "Admin"

        Log.d("AdminDashboard", "Admin user: $fullName")
        Toast.makeText(this, "Welcome, $fullName", Toast.LENGTH_SHORT).show()
    }

    private fun loadDashboardData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("AdminDashboard", "→ Fetching dashboard data")

                val request = Request.Builder()
                    .url("$BACKEND_URL/get_admin_dashboard_details.php")
                    .build()

                val response = client.newCall(request).execute()
                val jsonData = response.body?.string() ?: ""

                Log.d("AdminDashboard", "← Response Code: ${response.code}")
                Log.d("AdminDashboard", "← Response Body: $jsonData")

                val json = JSONObject(jsonData)
                val success = json.getBoolean("success")

                if (success) {
                    val stats = json.getJSONObject("stats")
                    val totalTeachers = stats.getInt("total_teachers")
                    val totalRooms = stats.getInt("total_rooms")
                    val totalSchedules = stats.getInt("total_schedules")

                    Log.d("AdminDashboard", "✓ Stats - Teachers: $totalTeachers, Rooms: $totalRooms, Schedules: $totalSchedules")

                    withContext(Dispatchers.Main) {
                        // Update UI with stats
                        findViewById<TextView>(R.id.tvPendingCount)?.text =
                            "$totalSchedules schedules in system"

                        Toast.makeText(this@AdminDashboardActivity,
                            "Dashboard loaded successfully", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val message = json.optString("message", "Failed to load dashboard")
                    Log.e("AdminDashboard", "✗ API error: $message")

                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@AdminDashboardActivity,
                            "Error: $message", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("AdminDashboard", "✗ Error loading dashboard: ${e.message}", e)

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AdminDashboardActivity,
                        "Error: ${e.message}", Toast.LENGTH_LONG).show()
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