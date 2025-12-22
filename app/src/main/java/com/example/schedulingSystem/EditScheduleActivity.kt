package com.example.schedulingSystem

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.example.schedulingSystem.models.*
import com.example.schedulingSystem.services.DropdownDataService
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class EditScheduleActivity : AppCompatActivity() {

    private lateinit var etDay: AutoCompleteTextView
    private lateinit var etRoomName: TextInputEditText
    private lateinit var etSubjectCode: AutoCompleteTextView
    private lateinit var etSubjectName: AutoCompleteTextView
    private lateinit var etSection: AutoCompleteTextView
    private lateinit var etTeacher: AutoCompleteTextView
    private lateinit var etStatus: AutoCompleteTextView
    private lateinit var etTimeStart: AutoCompleteTextView
    private lateinit var etTimeEnd: AutoCompleteTextView

    private val dropdownService = DropdownDataService()
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val BACKEND_URL = "http://10.0.2.2/scheduling-api"
    }

    // Data lists
    private var days: List<Day> = emptyList()
    private var teachers: List<Teacher> = emptyList()
    private var timeSlots: List<TimeSlot> = emptyList()
    private var subjects: List<Subject> = emptyList()
    private var sections: List<Section> = emptyList()

    // Selected IDs
    private var selectedDayId: Int = -1
    private var selectedTeacherId: Int = -1
    private var selectedSubjectId: Int = -1
    private var selectedSectionId: Int = -1
    private var selectedTimeStartId: Int = -1
    private var selectedTimeEndId: Int = -1

    // Schedule info from intent
    private var scheduleId: Int = -1
    private var roomId: Int = -1
    private var roomName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_schedule)

        // Get schedule info from intent
        scheduleId = intent.getIntExtra("schedule_id", -1)
        roomId = intent.getIntExtra("room_id", -1)
        roomName = intent.getStringExtra("room_name") ?: ""

        initViews()
        setupClickListeners()
        loadDropdownData()

        // Pre-fill room name (not editable as dropdown, since it's fixed for the room)
        etRoomName.setText(roomName)
        etRoomName.isEnabled = false  // Make it non-editable if room is fixed

        // Load schedule details if editing existing schedule
        if (scheduleId != -1) {
            loadScheduleDetails()
        }
    }

    private fun initViews() {
        etDay = findViewById(R.id.etEditScheduleDay)
        etRoomName = findViewById(R.id.etEditRoomName)
        etSubjectCode = findViewById(R.id.etSubjectCode)
        etSubjectName = findViewById(R.id.etSubjectName)
        etSection = findViewById(R.id.etSection)
        etTeacher = findViewById(R.id.etTeacher)
        etStatus = findViewById(R.id.etStatus)
        etTimeStart = findViewById(R.id.etTimeStart)
        etTimeEnd = findViewById(R.id.etTimeEnd)
    }

    private fun setupClickListeners() {
        findViewById<View>(R.id.btnCancelEdit).setOnClickListener {
            finish()
        }

        findViewById<View>(R.id.btnUpdateRoom).setOnClickListener {
            saveSchedule()
        }
    }

    private fun loadDropdownData() {
        CoroutineScope(Dispatchers.IO).launch {
            // Load all dropdown data
            val daysResult = dropdownService.getDays()
            val teachersResult = dropdownService.getTeachers()
            val timeSlotsResult = dropdownService.getTimeSlots()
            val subjectsResult = dropdownService.getSubjects()

            withContext(Dispatchers.Main) {
                daysResult.onSuccess { daysList ->
                    days = daysList
                    setupDayDropdown()
                }.onFailure {
                    Toast.makeText(this@EditScheduleActivity, "Failed to load days", Toast.LENGTH_SHORT).show()
                }

                teachersResult.onSuccess { teachersList ->
                    teachers = teachersList
                    setupTeacherDropdown()
                }.onFailure {
                    Toast.makeText(this@EditScheduleActivity, "Failed to load teachers", Toast.LENGTH_SHORT).show()
                }

                timeSlotsResult.onSuccess { timeSlotsList ->
                    timeSlots = timeSlotsList
                    setupTimeDropdowns()
                }.onFailure {
                    Toast.makeText(this@EditScheduleActivity, "Failed to load time slots", Toast.LENGTH_SHORT).show()
                }

                subjectsResult.onSuccess { subjectsList ->
                    subjects = subjectsList
                    setupSubjectDropdowns()
                }.onFailure {
                    Toast.makeText(this@EditScheduleActivity, "Failed to load subjects", Toast.LENGTH_SHORT).show()
                }

                // Setup status dropdown
                setupStatusDropdown()
            }
        }
    }

    private fun setupDayDropdown() {
        val dayNames = days.map { it.name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, dayNames)
        etDay.setAdapter(adapter)

        etDay.setOnItemClickListener { parent, _, position, _ ->
            selectedDayId = days[position].id
        }
    }

    private fun setupTeacherDropdown() {
        val teacherNames = teachers.map { "${it.name} (${it.email})" }
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, teacherNames)
        etTeacher.setAdapter(adapter)

        etTeacher.setOnItemClickListener { parent, _, position, _ ->
            selectedTeacherId = teachers[position].id
            loadSectionsForTeacherAndSubject()
        }
    }

    private fun setupTimeDropdowns() {
        val timeDisplayNames = timeSlots.map { it.displayName }
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, timeDisplayNames)

        etTimeStart.setAdapter(adapter)
        etTimeEnd.setAdapter(adapter)

        etTimeStart.setOnItemClickListener { parent, _, position, _ ->
            selectedTimeStartId = timeSlots[position].id
        }

        etTimeEnd.setOnItemClickListener { parent, _, position, _ ->
            selectedTimeEndId = timeSlots[position].id
        }
    }

    private fun setupSubjectDropdowns() {
        val subjectCodes = subjects.map { it.code }
        val subjectNames = subjects.map { it.name }

        etSubjectCode.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, subjectCodes))
        etSubjectName.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, subjectNames))

        etSubjectCode.setOnItemClickListener { parent, _, position, _ ->
            selectedSubjectId = subjects[position].id
            etSubjectName.setText(subjects[position].name, false)
            loadSectionsForTeacherAndSubject()
        }

        etSubjectName.setOnItemClickListener { parent, _, position, _ ->
            selectedSubjectId = subjects[position].id
            etSubjectCode.setText(subjects[position].code, false)
            loadSectionsForTeacherAndSubject()
        }
    }

    private fun loadSectionsForTeacherAndSubject(callback: (() -> Unit)? = null) {
        if (selectedTeacherId != -1 && selectedSubjectId != -1) {
            CoroutineScope(Dispatchers.IO).launch {
                val sectionsResult = dropdownService.getSections(selectedTeacherId, selectedSubjectId)

                withContext(Dispatchers.Main) {
                    sectionsResult.onSuccess { sectionsList ->
                        sections = sectionsList
                        setupSectionDropdown()
                        callback?.invoke()
                    }.onFailure {
                        Toast.makeText(this@EditScheduleActivity, "Failed to load sections", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun setupSectionDropdown() {
        val sectionNames = sections.map { "${it.name} - Year ${it.year}" }
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, sectionNames)
        etSection.setAdapter(adapter)

        etSection.setOnItemClickListener { parent, _, position, _ ->
            selectedSectionId = sections[position].id
        }
    }

    private fun setupStatusDropdown() {
        val statuses = arrayOf("Pending", "Occupied", "Available")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, statuses)
        etStatus.setAdapter(adapter)

        etStatus.setOnItemClickListener { parent, _, position, _ ->
            // Handle status selection if needed
        }
    }

    private fun loadScheduleDetails() {
        if (scheduleId == -1) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = "$BACKEND_URL/get_schedule_details.php?schedule_id=$scheduleId"
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                val body = response.body?.string() ?: ""

                val json = JSONObject(body)
                if (!json.getBoolean("success")) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@EditScheduleActivity, "Schedule not found", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                val schedule = json.getJSONObject("schedule")

                withContext(Dispatchers.Main) {
                    populateFormWithScheduleData(schedule)
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EditScheduleActivity, "Failed to load schedule: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun populateFormWithScheduleData(schedule: JSONObject) {
        try {
            // Set day
            val dayName = schedule.getString("day_name")
            val dayIndex = days.indexOfFirst { it.name.equals(dayName, ignoreCase = true) }
            if (dayIndex >= 0) {
                etDay.setText(days[dayIndex].name, false)
                selectedDayId = days[dayIndex].id
            }

            // Set subject
            val subjectCode = schedule.getString("subject_code")
            val subjectName = schedule.getString("subject_name")
            etSubjectCode.setText(subjectCode, false)
            etSubjectName.setText(subjectName, false)

            val subjectIndex = subjects.indexOfFirst { it.code.equals(subjectCode, ignoreCase = true) }
            if (subjectIndex >= 0) {
                selectedSubjectId = subjects[subjectIndex].id
            }

            // Set teacher
            val teacherName = schedule.getString("teacher_name")
            val teacherEmail = schedule.optString("teacher_email", "")  // Assuming email is in the JSON
            val teacherDisplay = "$teacherName ($teacherEmail)"
            val teacherIndex = teachers.indexOfFirst { "${it.name} (${it.email})".equals(teacherDisplay, ignoreCase = true) }
            if (teacherIndex >= 0) {
                etTeacher.setText(teacherDisplay, false)
                selectedTeacherId = teachers[teacherIndex].id
            }

            // After setting subject and teacher IDs, load sections and then set section
            loadSectionsForTeacherAndSubject {
                val sectionName = schedule.getString("section_name")
                val sectionYear = schedule.optInt("section_year", 0)
                val sectionIndex = sections.indexOfFirst {
                    it.name.equals(sectionName, ignoreCase = true) && it.year == sectionYear
                }
                if (sectionIndex >= 0) {
                    etSection.setText("${sections[sectionIndex].name} - Year ${sections[sectionIndex].year}", false)
                    selectedSectionId = sections[sectionIndex].id
                }
            }

            // Set time slots
            val timeStart = schedule.getString("time_start")
            val timeEnd = schedule.getString("time_end")

            val startTimeIndex = timeSlots.indexOfFirst { it.displayName.equals(timeStart, ignoreCase = true) }
            if (startTimeIndex >= 0) {
                etTimeStart.setText(timeSlots[startTimeIndex].displayName, false)
                selectedTimeStartId = timeSlots[startTimeIndex].id
            }

            val endTimeIndex = timeSlots.indexOfFirst { it.displayName.equals(timeEnd, ignoreCase = true) }
            if (endTimeIndex >= 0) {
                etTimeEnd.setText(timeSlots[endTimeIndex].displayName, false)
                selectedTimeEndId = timeSlots[endTimeIndex].id
            }

            // Set status
            val status = schedule.optString("schedule_status", "Pending")
            etStatus.setText(status, false)

        } catch (e: Exception) {
            Toast.makeText(this, "Error populating form: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun saveSchedule() {
        // Validate required fields
        if (selectedDayId == -1 || selectedTeacherId == -1 || selectedSubjectId == -1 ||
            selectedSectionId == -1 || selectedTimeStartId == -1 || selectedTimeEndId == -1 ||
            roomId == -1) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val status = etStatus.text.toString()

        // Build JSON payload
        val payload = JSONObject().apply {
            put("day_id", selectedDayId)
            put("subject_id", selectedSubjectId)
            put("section_id", selectedSectionId)
            put("teacher_id", selectedTeacherId)
            put("time_start_id", selectedTimeStartId)
            put("time_end_id", selectedTimeEndId)
            put("room_id", roomId)
            put("schedule_status", status)
            if (scheduleId != -1) {
                put("schedule_id", scheduleId)
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = if (scheduleId != -1) {
                    "$BACKEND_URL/update_schedule.php"
                } else {
                    "$BACKEND_URL/add_schedule.php"
                }
                val requestBody = payload.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build()
                val response = client.newCall(request).execute()
                val body = response.body?.string() ?: ""

                val json = JSONObject(body)
                val success = json.getBoolean("success")
                val message = json.optString("message", "Operation completed")

                withContext(Dispatchers.Main) {
                    if (success) {
                        Toast.makeText(this@EditScheduleActivity, "Schedule saved successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@EditScheduleActivity, "Save failed: $message", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EditScheduleActivity, "Save failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}