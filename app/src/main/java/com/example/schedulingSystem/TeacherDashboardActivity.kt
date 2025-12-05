package com.example.schedulingSystem

import com.example.schedulingSystem.models.RoomAvailability
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class TeacherDashboardActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: LinearLayout
    private lateinit var rvRooms: RecyclerView
    private lateinit var tvProfName: TextView
    private lateinit var tvGreeting: TextView
    private lateinit var btnOpenDrawer: ImageView

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val roomAdapter = TeacherRoomAdapter()

    companion object {
        private const val BACKEND_URL = "http://10.0.2.2/scheduling-api"
        private const val TAG = "TeacherDashboard"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "=== onCreate started ===")

        // Check if user is logged in and is teacher
        val prefs = getSharedPreferences("user_session", MODE_PRIVATE)
        val isLoggedIn = prefs.getBoolean("is_logged_in", false)
        val accountType = prefs.getString("account_type", "")

        Log.d(TAG, "isLoggedIn: $isLoggedIn, accountType: $accountType")

        if (!isLoggedIn) {
            Log.w(TAG, "User not logged in, redirecting to login")
            redirectToLogin()
            return
        }

        if (accountType?.lowercase() != "teacher") {
            Log.w(TAG, "User is not teacher (type: $accountType), redirecting to login")
            redirectToLogin()
            return
        }

        try {
            Log.d(TAG, "Setting content view...")
            setContentView(R.layout.activity_teacher_dashboard)
            Log.d(TAG, "✓ Layout inflated successfully!")

            initViews()
            setupDrawer()
            setupBackPressHandler()
            updateUserName()
            setupRecyclerView()
            loadRoomsFromApi()

            Log.d(TAG, "✓ TeacherDashboard initialized successfully!")
        } catch (e: Exception) {
            Log.e(TAG, "✗ FATAL ERROR in onCreate: ${e.message}", e)
            e.printStackTrace()
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            redirectToLogin()
        }
    }

    private fun initViews() {
        try {
            Log.d(TAG, "Initializing views...")
            drawerLayout = findViewById(R.id.drawerLayout)
            navView = findViewById(R.id.navView)
            rvRooms = findViewById(R.id.rvRooms)
            tvProfName = findViewById(R.id.tvProfName)
            tvGreeting = findViewById(R.id.tvGreeting)
            btnOpenDrawer = findViewById(R.id.btnOpenDrawer)

            Log.d(TAG, "✓ All views initialized")
        } catch (e: Exception) {
            Log.e(TAG, "✗ Error initializing views: ${e.message}", e)
            throw e
        }
    }

    private fun setupDrawer() {
        try {
            Log.d(TAG, "Setting up drawer...")

            // Open drawer button
            btnOpenDrawer.setOnClickListener {
                Log.d(TAG, "Opening drawer")
                drawerLayout.openDrawer(GravityCompat.START)
            }

            // Update drawer header
            updateDrawerHeader()

            // Setup menu item click listeners
            navView.findViewById<LinearLayout>(R.id.nav_home)?.setOnClickListener {
                Log.d(TAG, "Home clicked")
                drawerLayout.closeDrawer(GravityCompat.START)
            }

            navView.findViewById<LinearLayout>(R.id.nav_my_requests)?.setOnClickListener {
                Log.d(TAG, "My Requests clicked")
                Toast.makeText(this, "My Requests - Coming soon", Toast.LENGTH_SHORT).show()
                drawerLayout.closeDrawer(GravityCompat.START)
            }

            navView.findViewById<LinearLayout>(R.id.nav_profile)?.setOnClickListener {
                Log.d(TAG, "Profile clicked")
                Toast.makeText(this, "Profile - Coming soon", Toast.LENGTH_SHORT).show()
                drawerLayout.closeDrawer(GravityCompat.START)
            }

            navView.findViewById<LinearLayout>(R.id.nav_logout)?.setOnClickListener {
                Log.d(TAG, "Logout clicked")
                performLogout()
            }

            Log.d(TAG, "✓ Drawer setup complete")
        } catch (e: Exception) {
            Log.e(TAG, "✗ Error setting up drawer: ${e.message}", e)
        }
    }

    private fun setupBackPressHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun updateUserName() {
        val prefs = getSharedPreferences("user_session", MODE_PRIVATE)
        val fullName = prefs.getString("full_name", "Teacher") ?: "Teacher"
        tvGreeting.text = "Welcome back,"
        tvProfName.text = fullName
        Log.d(TAG, "✓ Updated user name: $fullName")
    }

    private fun updateDrawerHeader() {
        try {
            val prefs = getSharedPreferences("user_session", MODE_PRIVATE)
            val fullName = prefs.getString("full_name", "Teacher") ?: "Teacher"
            val username = prefs.getString("username", "") ?: ""

            navView.findViewById<TextView>(R.id.tvNavName)?.text = fullName
            navView.findViewById<TextView>(R.id.tvNavEmail)?.text = username

            Log.d(TAG, "✓ Drawer header updated: $fullName")
        } catch (e: Exception) {
            Log.e(TAG, "✗ Error updating drawer header: ${e.message}", e)
        }
    }

    private fun setupRecyclerView() {
        rvRooms.layoutManager = LinearLayoutManager(this)
        rvRooms.adapter = roomAdapter
        Log.d(TAG, "✓ RecyclerView setup complete")
    }

    private fun loadRoomsFromApi() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "→ Loading rooms from API...")

                val request = Request.Builder()
                    .url("$BACKEND_URL/get_rooms.php")
                    .build()

                val response = client.newCall(request).execute()
                val jsonData = response.body?.string() ?: ""

                Log.d(TAG, "← API Response: ${jsonData.take(100)}...")

                if (jsonData.isEmpty()) {
                    Log.w(TAG, "⚠ Empty response from API")
                    return@launch
                }

                val json = JSONObject(jsonData)
                val success = json.optBoolean("success", false)

                if (success) {
                    val roomsArray = json.optJSONArray("rooms")
                    if (roomsArray != null) {
                        val list = mutableListOf<RoomAvailability>()

                        for (i in 0 until roomsArray.length()) {
                            try {
                                val obj = roomsArray.getJSONObject(i)
                                list.add(
                                    RoomAvailability(
                                        roomId = obj.getInt("id"),
                                        roomName = obj.getString("name"),
                                        roomCapacity = obj.getInt("capacity"),
                                        status = obj.optString("status", "Available"),
                                        isAvailable = obj.optBoolean("isAvailable", true)
                                    )
                                )
                            } catch (e: Exception) {
                                Log.e(TAG, "✗ Error parsing room $i: ${e.message}")
                            }
                        }

                        withContext(Dispatchers.Main) {
                            roomAdapter.submitList(list)
                            Log.d(TAG, "✓ Loaded ${list.size} rooms")
                        }
                    } else {
                        Log.w(TAG, "⚠ No rooms array in response")
                    }
                } else {
                    val message = json.optString("message", "Failed")
                    Log.e(TAG, "✗ API error: $message")
                }
            } catch (e: Exception) {
                Log.e(TAG, "✗ Error loading rooms: ${e.message}", e)
            }
        }
    }

    private fun performLogout() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                Log.d(TAG, "User confirmed logout")
                getSharedPreferences("user_session", MODE_PRIVATE).edit { clear() }
                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
                redirectToLogin()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun redirectToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}