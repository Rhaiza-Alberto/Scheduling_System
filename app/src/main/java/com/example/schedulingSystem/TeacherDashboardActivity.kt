package com.example.schedulingSystem

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import androidx.core.content.edit

data class Room(
    val id: Int,
    val name: String,
    val capacity: Int,
    val status: String,
    val isAvailable: Boolean
)

class TeacherDashboardActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var rvRooms: RecyclerView
    private lateinit var tvProfName: TextView
    private lateinit var tvGreeting: TextView
    private lateinit var btnOpenDrawer: ImageView

    private val client = OkHttpClient()
    private val roomAdapter = TeacherRoomAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacher_dashboard)

        initViews()
        setupDrawer()
        updateUserName()        // This line makes the magic happen
        setupRecyclerView()
        loadRoomsFromApi()     // or loadMockData()
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
    }

    @SuppressLint("SetTextI18n")
    private fun loadUserData() {
        val prefs = getSharedPreferences("user_session", MODE_PRIVATE)
        val firstName = prefs.getString("first_name", "Teacher") ?: "Teacher"
        val lastName = prefs.getString("last_name", "") ?: ""

        tvGreeting.text = "Welcome back,"
        tvProfName.text = "$firstName $lastName".trim()

        // Update drawer header
        val header = navView.getHeaderView(0)
        header.findViewById<TextView>(R.id.tvNavName)?.text = "$firstName $lastName".trim()
        header.findViewById<TextView>(R.id.tvNavEmail)?.text = prefs.getString("username", "")
    }

    private fun setupRecyclerView() {
        rvRooms.layoutManager = LinearLayoutManager(this)
       // rvRooms.adapter = roomAdapter
    }

    private fun loadRoomsFromApi() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = Request.Builder()
                    .url("http://10.0.2.2/scheduling-api/get_rooms.php") // Emulator
                    // .url("http://192.168.x.x/scheduling-api/get_rooms.php") // Real device
                    .build()

                val response = client.newCall(request).execute()
                val jsonData = response.body?.string() ?: ""

                val json = JSONObject(jsonData)
                val success = json.getBoolean("success")

                if (success) {
                    val roomsArray = json.getJSONArray("rooms")
                    val roomList = mutableListOf<Room>()

                    for (i in 0 until roomsArray.length()) {
                        val obj = roomsArray.getJSONObject(i)
                        roomList.add(
                            Room(
                                id = obj.getInt("id"),
                                name = obj.getString("name"),
                                capacity = obj.getInt("capacity"),
                                status = obj.getString("status"),
                                isAvailable = obj.getBoolean("isAvailable")
                            )
                        )
                    }

//                    withContext(Dispatchers.Main) {
//                        roomAdapter.submitList(roomList)
//                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@TeacherDashboardActivity, "Failed to load rooms", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@TeacherDashboardActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_logout -> logout()
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    @SuppressLint("SetTextI18n")
    private fun updateUserName() {
        val prefs = getSharedPreferences("user_session", MODE_PRIVATE)
        val firstName = prefs.getString("first_name", "User") ?: "User"
        val lastName = prefs.getString("last_name", "") ?: ""

        val fullName = "$firstName $lastName".trim()

        tvGreeting.text = "Welcome back,"
        tvProfName.text = fullName

        // Also update Navigation Drawer header (optional but nice)
        val headerView = navView.getHeaderView(0)
        headerView.findViewById<TextView>(R.id.tvNavName)?.text = fullName
        headerView.findViewById<TextView>(R.id.tvNavEmail)?.text = prefs.getString("username", "")
    }



    private fun logout() {
        getSharedPreferences("user_session", MODE_PRIVATE).edit { clear() }
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }


}