package com.example.schedulingSystem

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.schedulingSystem.adapters.AdminRoomScheduleAdapter
import com.example.schedulingSystem.models.RoomItem
import com.google.android.material.button.MaterialButton
import com.google.android.material.materialswitch.MaterialSwitch
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class AdminDashboardActivity : AppCompatActivity() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val BACKEND_URL = "http://10.0.2.2/scheduling-api"
    }

    // UI References
    private lateinit var roomAdapter: AdminRoomScheduleAdapter
    private lateinit var rvRoomsWeek: RecyclerView        // Week view (horizontal)
    private lateinit var rvRoomsDay: RecyclerView         // Day view (vertical)

    // Toggle & Day Selector
    private lateinit var switchViewMode: MaterialSwitch
    private lateinit var spinnerDaySelect: Spinner
    private lateinit var tvDayHeader: TextView

    // Containers
    private lateinit var weeklyContainer: View
    private lateinit var dailyContainer: View

    private var allRooms = listOf<RoomItem>()
    private var currentDay = "Monday"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // === Security Check ===
        val prefs = getSharedPreferences("user_session", MODE_PRIVATE)
        if (!prefs.getBoolean("is_logged_in", false) ||
            prefs.getString("account_type", "").orEmpty().lowercase() != "admin"
        ) {
            Toast.makeText(this, "Access denied: Admin only", Toast.LENGTH_LONG).show()
            redirectToLogin()
            return
        }

        setContentView(R.layout.activity_admin_dashboard)

        initViews()
        setupRecyclerViews()
        setupClickListeners()
        displayUserInfo()
        loadRoomsFromApi()  // This loads data once → used for both views
    }

    private fun initViews() {
        // Toggle switch & spinner
        switchViewMode = findViewById(R.id.switchViewMode)
        spinnerDaySelect = findViewById(R.id.spinnerDaySelect)
        tvDayHeader = findViewById(R.id.tvDayHeader)

        // Containers from FrameLayout
        weeklyContainer = findViewById(R.id.horizontalScrollWeekly)   // Correct!
        dailyContainer = findViewById(R.id.scrollDailyView)

        // RecyclerViews from included layouts
        rvRoomsWeek = weeklyContainer.findViewById(R.id.containerRooms)
        rvRoomsDay = dailyContainer.findViewById(R.id.containerRoomsDay)
    }

    private fun setupRecyclerViews() {
        // Week View RecyclerView
        rvRoomsWeek.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        roomAdapter = AdminRoomScheduleAdapter { room ->
            val intent = Intent(this, AdminDashboardActivity::class.java).apply {
                putExtra("room_id", room.roomId)
                putExtra("room_name", room.roomName)
            }
            startActivity(intent)
        }
        rvRoomsWeek.adapter = roomAdapter

        // Day View RecyclerView (vertical)
        rvRoomsDay.layoutManager = LinearLayoutManager(this)
        rvRoomsDay.adapter = roomAdapter  // Same adapter works! Just different layout manager
    }

    private fun setupClickListeners() {
        // Settings → Logout
        findViewById<ImageButton>(R.id.btnSettings).setOnClickListener { performLogout() }

        // Review Button
        findViewById<MaterialButton>(R.id.btnReview)?.setOnClickListener {
            Toast.makeText(this, "Review pending approvals - Coming soon", Toast.LENGTH_SHORT).show()
        }

        // === TAB NAVIGATION ===
        val tabSchedules = findViewById<TextView>(R.id.tabSchedules)
        val tabUsers = findViewById<TextView>(R.id.tabUsers)
        val tabRooms = findViewById<TextView>(R.id.tabRooms)

        tabSchedules.setOnClickListener { updateTabSelection(it as TextView, tabUsers, tabRooms) }
        tabUsers.setOnClickListener {
            updateTabSelection(it as TextView, tabSchedules, tabRooms)
            startActivity(Intent(this, AdminManageUsersActivity::class.java))
            finish()
        }
        tabRooms.setOnClickListener {
            updateTabSelection(it as TextView, tabSchedules, tabUsers)
            startActivity(Intent(this, AdminManageRoomsActivity::class.java))
            finish()
        }
        updateTabSelection(tabSchedules, tabUsers, tabRooms)

        // === WEEK / DAY TOGGLE ===
        switchViewMode.setOnCheckedChangeListener { _, isDayMode ->
            if (isDayMode) {
                weeklyContainer.visibility = View.GONE
                dailyContainer.visibility = View.VISIBLE
                spinnerDaySelect.visibility = View.VISIBLE

                currentDay = spinnerDaySelect.selectedItem.toString()
                tvDayHeader.text = currentDay.uppercase()
                updateDayView()
            } else {
                weeklyContainer.visibility = View.VISIBLE
                dailyContainer.visibility = View.GONE
                spinnerDaySelect.visibility = View.GONE
                updateWeekView()
            }
        }

        spinnerDaySelect.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                if (switchViewMode.isChecked) {
                    currentDay = resources.getStringArray(R.array.day_of_week)[pos]
                    tvDayHeader.text = currentDay.uppercase()
                    updateDayView()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun updateWeekView() {
        roomAdapter.submitList(allRooms)
        rvRoomsWeek.scrollToPosition(0)
    }

    private fun updateDayView() {
        // Optional: filter rooms by availability on currentDay (if you have schedule data per day)
        // For now, just show all rooms (same as week view)
        roomAdapter.submitList(allRooms)
        rvRoomsDay.scrollToPosition(0)
    }

    private fun loadRoomsFromApi() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = client.newCall(
                    Request.Builder().url("$BACKEND_URL/get_rooms.php").build()
                ).execute()

                val json = JSONObject(response.body?.string() ?: "{}")
                if (json.getBoolean("success")) {
                    val roomsArray = json.getJSONArray("rooms")
                    val roomList = mutableListOf<RoomItem>()

                    for (i in 0 until roomsArray.length()) {
                        val obj = roomsArray.getJSONObject(i)
                        roomList.add(
                            RoomItem(
                                roomId = obj.getInt("id"),
                                roomName = obj.getString("name"),
                                roomCapacity = obj.getInt("capacity")
                            )
                        )
                    }

                    allRooms = roomList

                    withContext(Dispatchers.Main) {
                        updateWeekView()  // Default view
                        Toast.makeText(
                            this@AdminDashboardActivity,
                            "Loaded ${roomList.size} rooms",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AdminDashboardActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun updateTabSelection(selected: TextView, vararg others: TextView) {
        selected.apply {
            setBackgroundResource(R.drawable.bg_input_outline)
            backgroundTintList = ContextCompat.getColorStateList(this@AdminDashboardActivity, R.color.white)
            setTextColor(ContextCompat.getColor(this@AdminDashboardActivity, R.color.primary_dark_green))
            setTypeface(null, Typeface.BOLD)
            elevation = 4f
        }
        others.forEach {
            it.apply {
                background = null
                setTextColor(ContextCompat.getColor(this@AdminDashboardActivity, R.color.white))
                setTypeface(null, Typeface.NORMAL)
                elevation = 0f
            }
        }
    }

    private fun displayUserInfo() {
        val name = getSharedPreferences("user_session", MODE_PRIVATE)
            .getString("full_name", "Admin") ?: "Admin"
        Toast.makeText(this, "Welcome, $name", Toast.LENGTH_SHORT).show()
    }

    private fun performLogout() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure?")
            .setPositiveButton("Yes") { _, _ ->
                getSharedPreferences("user_session", MODE_PRIVATE).edit { clear() }
                redirectToLogin()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun redirectToLogin() {
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}