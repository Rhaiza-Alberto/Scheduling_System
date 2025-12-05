package com.example.schedulingSystem

import com.example.schedulingSystem.models.RoomAvailability
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class TeacherDashboardActivity : AppCompatActivity(),
    NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
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
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacher_dashboard)

        initViews()
        setupDrawer()
        setupBackPressHandler()
        updateUserName()
        setupRecyclerView()
        loadRoomsFromApi()
    }

    private fun initViews() {
        drawerLayout = findViewById(R.id.drawerLayout)
        navView = findViewById(R.id.navView)
        rvRooms = findViewById(R.id.rvRooms)
        tvProfName = findViewById(R.id.tvProfName)
        tvGreeting = findViewById(R.id.tvGreeting)
        btnOpenDrawer = findViewById(R.id.btnOpenDrawer)
    }

    private fun setupDrawer() {
        btnOpenDrawer.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        navView.setNavigationItemSelectedListener(this)
        updateDrawerHeader()
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
    }

    private fun updateDrawerHeader() {
        val prefs = getSharedPreferences("user_session", MODE_PRIVATE)
        val fullName = prefs.getString("full_name", "Teacher") ?: "Teacher"
        val username = prefs.getString("username", "") ?: ""

        val headerView = navView.getHeaderView(0)
        headerView.findViewById<TextView>(R.id.tvNavName).text = fullName
        headerView.findViewById<TextView>(R.id.tvNavEmail).text = username
    }

    private fun setupRecyclerView() {
        rvRooms.layoutManager = LinearLayoutManager(this)
        rvRooms.adapter = roomAdapter
    }

    private fun loadRoomsFromApi() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = Request.Builder()
                    .url("$BACKEND_URL/get_rooms.php")
                    .build()

                val response = client.newCall(request).execute()
                val jsonData = response.body?.string() ?: ""

                if (jsonData.isEmpty()) return@launch

                val json = JSONObject(jsonData)
                if (json.getBoolean("success")) {
                    val roomsArray = json.getJSONArray("rooms")
                    val list = mutableListOf<RoomAvailability>()

                    for (i in 0 until roomsArray.length()) {
                        val obj = roomsArray.getJSONObject(i)
                        list.add(
                            RoomAvailability(
                                roomId = obj.getInt("room_ID"),
                                roomName = obj.getString("room_name"),
                                roomCapacity = obj.getInt("room_capacity"),
                                status = obj.optString("status", "Available"),
                                isAvailable = obj.optBoolean("isAvailable", true)
                            )
                        )
                    }

                    withContext(Dispatchers.Main) {
                        roomAdapter.submitList(list)
                    }
                }
            } catch (e: Exception) {
                Log.e("API", "Error: ${e.message}")
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> { }
            R.id.nav_my_requests -> { }
            R.id.nav_profile -> { }
            R.id.nav_logout -> performLogout()
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun performLogout() {
        getSharedPreferences("user_session", MODE_PRIVATE).edit { clear() }
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}