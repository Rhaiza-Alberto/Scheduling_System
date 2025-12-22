package com.example.schedulingSystem

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.schedulingSystem.models.ScheduleEntry
import com.example.schedulingSystem.models.TimetableItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.Calendar
import java.util.concurrent.TimeUnit
import com.example.schedulingSystem.adapters.TimetableGridAdapter
import com.example.schedulingSystem.adapters.OnScheduleClickListener

class AdminDashboardScheduleRoom : AppCompatActivity(), OnScheduleClickListener {

    private lateinit var rvTimetable: RecyclerView
    private lateinit var tvRoomName: TextView
    private lateinit var btnWeekView: Button // New
    private lateinit var btnDayView: Button  // New

    // --- View State ---
    private var isWeekView: Boolean = true // Default to Week View
    private var currentDay: String = "" // Tracks the selected day for Day View (e.g., "Monday")
    private var allSchedules: List<ScheduleEntry> = emptyList() // Store full schedule
    private var roomId: Int = -1 // Store room ID for editing
    private var roomName: String = "" // Store room name for editing

    // 7:00 AM to 7:00 PM → 25 slots (inclusive)
    private val timeSlots = listOf(
        "7:00 AM", "7:30 AM", "8:00 AM", "8:30 AM", "9:00 AM", "9:30 AM",
        "10:00 AM", "10:30 AM", "11:00 AM", "11:30 AM", "12:00 PM", "12:30 PM",
        "1:00 PM", "1:30 PM", "2:00 PM", "2:30 PM", "3:00 PM", "3:30 PM",
        "4:00 PM", "4:30 PM", "5:00 PM", "5:30 PM", "6:00 PM", "6:30 PM", "7:00 PM"
    )

    private val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val BACKEND_URL = "http://10.0.2.2/scheduling-api"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_room_schedule)

        val roomId = intent.getIntExtra("room_id", -1)
        val roomName = intent.getStringExtra("room_name") ?: "Room Schedule"

        // Store in class variables
        this.roomId = roomId
        this.roomName = roomName

        tvRoomName = findViewById(R.id.tvRoomName)
        rvTimetable = findViewById(R.id.rvTimetable)
        btnWeekView = findViewById(R.id.btnWeekView) // Initialize button
        btnDayView = findViewById(R.id.btnDayView)   // Initialize button

        tvRoomName.text = roomName

        // Initialize currentDay to today's day name
        currentDay = getCurrentDayName()

        // Setup listeners for the new buttons
        btnWeekView.setOnClickListener {
            if (!isWeekView) {
                isWeekView = true
                updateViewToggleUI()
                setupTimetableGrid()
                populateTimetable(allSchedules) // Repopulate with full data
            }
        }

        btnDayView.setOnClickListener {
            if (isWeekView) {
                isWeekView = false
                updateViewToggleUI()
                setupTimetableGrid()
                setupDayView(currentDay) // Filter data for the current day
            }
        }

        // Initial UI and data load
        updateViewToggleUI()
        setupTimetableGrid()
        loadRoomSchedule(roomId)

        findViewById<ImageButton>(R.id.btnSettings).setOnClickListener {
            performLogout()
        }
    }

    private fun getCurrentDayName(): String {
        val calendar = Calendar.getInstance()
        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "Monday"
            Calendar.TUESDAY -> "Tuesday"
            Calendar.WEDNESDAY -> "Wednesday"
            Calendar.THURSDAY -> "Thursday"
            Calendar.FRIDAY -> "Friday"
            Calendar.SATURDAY -> "Saturday"
            Calendar.SUNDAY -> "Sunday"
            else -> "Monday" // Default case
        }
    }

    private fun updateViewToggleUI() {
        // Update button background and text color based on the current view
        if (isWeekView) {
            btnWeekView.setBackgroundResource(R.drawable.bg_toggle_active)
            btnWeekView.setTextColor(getColor(R.color.white))
            btnDayView.setBackgroundResource(android.R.color.transparent)
            btnDayView.setTextColor(getColor(R.color.primary_green))
        } else {
            btnWeekView.setBackgroundResource(android.R.color.transparent)
            btnWeekView.setTextColor(getColor(R.color.primary_green))
            btnDayView.setBackgroundResource(R.drawable.bg_toggle_active)
            btnDayView.setTextColor(getColor(R.color.white))
        }
    }

    private fun setupTimetableGrid() {
        val adapter = TimetableGridAdapter(this)
        rvTimetable.adapter = adapter

        // Change the grid columns and headers based on the view mode
        val numColumns = if (isWeekView) 8 else 2 // Week: 1 time + 7 days, Day: 1 time + 1 day
        val layoutManager = GridLayoutManager(this, numColumns)

        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                // Headers (first row) and Time labels (first column) always take 1 column
                return if (position % numColumns == 0 || position < numColumns) 1 else 1
            }
        }
        rvTimetable.layoutManager = layoutManager

        // Build empty grid first
        val list = mutableListOf<TimetableItem>()

        // Header row
        list.add(TimetableItem.Header("")) // top-left corner
        if (isWeekView) {
            days.forEach { list.add(TimetableItem.Header(it.substring(0, 3).uppercase())) } // Use 3-letter day
        } else {
            list.add(TimetableItem.Header(currentDay.uppercase())) // Only the current day
        }

        // Time rows
        timeSlots.forEach { time ->
            list.add(TimetableItem.TimeLabel(time))
            repeat(numColumns - 1) { list.add(TimetableItem.Empty) } // -1 for the time label column
        }

        adapter.submitList(list)
    }

    private fun loadRoomSchedule(roomId: Int) {
        if (roomId <= 0) {
            Toast.makeText(this, "Invalid room", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = "$BACKEND_URL/get_all_schedules.php?room_id=$roomId"
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                val body = response.body?.string() ?: ""

                val json = JSONObject(body)
                if (!json.getBoolean("success")) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@AdminDashboardScheduleRoom, "No schedules found", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                val schedules = json.getJSONArray("schedules")
                val scheduleList = mutableListOf<ScheduleEntry>()

                for (i in 0 until schedules.length()) {
                    val s = schedules.getJSONObject(i)
                    scheduleList.add(
                        ScheduleEntry(
                            scheduleId = s.getInt("schedule_ID"),
                            dayName = s.getString("day_name"),
                            startDisplay = s.getString("time_start"),
                            endDisplay = s.getString("time_end"),
                            subject = s.getString("subject_name"),
                            section = s.getString("section_name"),
                            teacher = s.getString("teacher_name"),
                            status = s.optString("schedule_status", "Pending")
                        )
                    )
                }

                allSchedules = scheduleList 

                withContext(Dispatchers.Main) {
                    if (isWeekView) {
                        populateTimetable(allSchedules)
                    } else {
                        // If defaulting to Day view, filter and display that day
                        setupDayView(currentDay)
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AdminDashboardScheduleRoom, "Load failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setupDayView(dayName: String) {
        // Filter the complete schedule list for the selected day
        val daySchedules = allSchedules.filter { it.dayName == dayName }
        populateTimetable(daySchedules)
    }


    private fun populateTimetable(schedules: List<ScheduleEntry>) {
        val adapter = rvTimetable.adapter as? TimetableGridAdapter ?: return
        val currentList = adapter.currentList.toMutableList()

        // 1. Clear existing schedule entries (only keep time labels and headers)
        // This is crucial when switching between views or repopulating.
        val numColumns = if (isWeekView) 8 else 2

        for (i in 0 until currentList.size) {
            // Keep Headers (index < numColumns) and TimeLabels (index % numColumns == 0)
            if (i >= numColumns && i % numColumns != 0) {
                currentList[i] = TimetableItem.Empty
            }
        }

        // 2. Insert new schedule entries by splitting time ranges into individual slots
        schedules.forEach { entry ->
            // Determine the column index based on view mode
            val dayColumnIndex = if (isWeekView) {
                days.indexOf(entry.dayName) + 1 // +1 for the time label column
            } else {
                // Day View: It's always in the single schedule column (index 1)
                1
            }

            // If in Day View and the entry is NOT for the current day, skip it
            if (!isWeekView && entry.dayName != currentDay) return@forEach

            // If the day is not found (and we're in Week view), skip it
            if (dayColumnIndex <= 0 && isWeekView) return@forEach

            val startRow = timeSlots.indexOf(entry.startDisplay)
            val endRow = timeSlots.indexOf(entry.endDisplay)
            if (startRow == -1 || endRow == -1 || startRow >= endRow) return@forEach

            // Fill each individual 30-minute slot instead of creating a span
            for (row in startRow until endRow) {
                // Grid position: (Header Row + (Row * numColumns)) + Column Index
                val position = numColumns + (row * numColumns) + dayColumnIndex
                
                // Fill each time slot with the same class information
                currentList[position] = TimetableItem.ClassBlock(
                    scheduleId = entry.scheduleId,
                    subject = entry.subject,
                    section = entry.section,
                    teacher = entry.teacher,
                    rowSpan = 1, // Each slot is now individual (rowSpan = 1)
                    status = entry.status,
                    dayName = entry.dayName,
                    timeSlot = timeSlots[row]
                )
            }
        }

        adapter.submitList(currentList)
    }

    private fun performLogout() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure?")
            .setPositiveButton("Yes") { _, _ ->
                getSharedPreferences("user_session", MODE_PRIVATE).edit { clear() }
                startActivity(Intent(this, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                finish()
            }
            .setNegativeButton("No", null)
            .show()
    }

    override fun onScheduleClick(scheduleId: Int, dayName: String, timeSlot: String, status: String) {
        // Handle schedule click - open edit dialog with schedule ID
        showEditScheduleDialog(scheduleId, dayName, timeSlot, status)
    }

    override fun onEmptySlotClick(dayName: String, timeSlot: String) {
        // Handle empty slot click - open create dialog
        showEditScheduleDialog(-1, dayName, timeSlot, "")
    }

    private fun showEditScheduleDialog(scheduleId: Int, dayName: String, timeSlot: String, status: String) {
        val intent = Intent(this, EditScheduleActivity::class.java).apply {
            putExtra("schedule_id", scheduleId)
            putExtra("room_id", roomId)
            putExtra("room_name", roomName)
            putExtra("day_name", dayName)
            putExtra("time_slot", timeSlot)
            putExtra("status", status)
        }
        startActivity(intent)
    }
}