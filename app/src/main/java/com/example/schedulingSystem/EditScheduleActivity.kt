package com.example.schedulingSystem

import android.os.Bundle
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.schedulingSystem.adapters.DropdownAdapter
import com.example.schedulingSystem.models.*
import com.example.schedulingSystem.services.DropdownDataService
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
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

    // AutoCompleteTextViews
    private lateinit var etDay: AutoCompleteTextView
    private lateinit var etRoomName: AutoCompleteTextView
    private lateinit var etSubjectCode: AutoCompleteTextView
    private lateinit var etSubjectName: AutoCompleteTextView
    private lateinit var etSection: AutoCompleteTextView
    private lateinit var etTeacher: AutoCompleteTextView
    private lateinit var etStatus: AutoCompleteTextView
    private lateinit var etTimeStart: AutoCompleteTextView
    private lateinit var etTimeEnd: AutoCompleteTextView

    // TextInputLayouts for error handling
    private lateinit var dayInputLayout: TextInputLayout
    private lateinit var roomNameInputLayout: TextInputLayout
    private lateinit var subjectCodeInputLayout: TextInputLayout
    private lateinit var subjectNameInputLayout: TextInputLayout
    private lateinit var sectionInputLayout: TextInputLayout
    private lateinit var teacherInputLayout: TextInputLayout
    private lateinit var statusInputLayout: TextInputLayout
    private lateinit var timeStartInputLayout: TextInputLayout
    private lateinit var timeEndInputLayout: TextInputLayout

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
    private var rooms: List<Room> = emptyList()
    private var teachers: List<Teacher> = emptyList()
    private var timeSlots: List<TimeSlot> = emptyList()
    private var subjects: List<Subject> = emptyList()
    private var sections: List<Section> = emptyList()

    // Selected IDs
    private var selectedDayId: Int = -1
    private var selectedRoomId: Int = -1
    private var selectedTeacherId: Int = -1
    private var selectedSubjectId: Int = -1
    private var selectedSectionId: Int = -1
    private var selectedTimeStartId: Int = -1
    private var selectedTimeEndId: Int = -1

    // Schedule info from intent
    private var scheduleId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_schedule)

        // Get schedule info from intent
        scheduleId = intent.getIntExtra("schedule_id", -1)

        initViews()
        setupClickListeners()
        loadDropdownData()

        // Load schedule details if editing existing schedule
        if (scheduleId != -1) {
            loadScheduleDetails()
        }
    }

    private fun initViews() {
        // Initialize AutoCompleteTextViews
        etDay = findViewById(R.id.dayAutoComplete)
        etRoomName = findViewById(R.id.roomNameAutoComplete)
        etSubjectCode = findViewById(R.id.subjectCodeAutoComplete)
        etSubjectName = findViewById(R.id.subjectNameAutoComplete)
        etSection = findViewById(R.id.sectionAutoComplete)
        etTeacher = findViewById(R.id.teacherAutoComplete)
        etStatus = findViewById(R.id.statusAutoComplete)
        etTimeStart = findViewById(R.id.timeStartAutoComplete)
        etTimeEnd = findViewById(R.id.timeEndAutoComplete)

        // Initialize TextInputLayouts
        dayInputLayout = findViewById(R.id.dayInputLayout)
        roomNameInputLayout = findViewById(R.id.roomNameInputLayout)
        subjectCodeInputLayout = findViewById(R.id.subjectCodeInputLayout)
        subjectNameInputLayout = findViewById(R.id.subjectNameInputLayout)
        sectionInputLayout = findViewById(R.id.sectionInputLayout)
        teacherInputLayout = findViewById(R.id.teacherInputLayout)
        statusInputLayout = findViewById(R.id.statusInputLayout)
        timeStartInputLayout = findViewById(R.id.timeStartInputLayout)
        timeEndInputLayout = findViewById(R.id.timeEndInputLayout)
    }

    private fun setupClickListeners() {
        findViewById<View>(R.id.btnCancelEdit).setOnClickListener {
            finish()
        }

        findViewById<View>(R.id.btnUpdateRoom).setOnClickListener {
            if (validateInputs()) {
                saveSchedule()
            }
        }
    }

    private fun loadDropdownData() {
        CoroutineScope(Dispatchers.IO).launch {
            // Load all dropdown data
            val daysResult = dropdownService.getDays()
            val roomsResult = dropdownService.getRooms()
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

                roomsResult.onSuccess { roomsList ->
                    rooms = roomsList
                    setupRoomDropdown()
                }.onFailure {
                    Toast.makeText(this@EditScheduleActivity, "Failed to load rooms", Toast.LENGTH_SHORT).show()
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
        val adapter = DropdownAdapter(this, dayNames)
        etDay.setAdapter(adapter)

        // Show dropdown when clicked
        etDay.setOnClickListener {
            etDay.showDropDown()
        }

        etDay.setOnItemClickListener { parent, _, position, _ ->
            selectedDayId = days[position].id
            dayInputLayout.error = null // Clear error on selection
        }
    }

    private fun setupRoomDropdown() {
        val roomNames = rooms.map { it.name }
        val adapter = DropdownAdapter(this, roomNames)
        etRoomName.setAdapter(adapter)

        // Show dropdown when clicked
        etRoomName.setOnClickListener {
            etRoomName.showDropDown()
        }

        etRoomName.setOnItemClickListener { parent, _, position, _ ->
            selectedRoomId = rooms[position].id
            roomNameInputLayout.error = null // Clear error on selection
        }
    }

    private fun setupTeacherDropdown() {
        val teacherNames = teachers.map { "${it.name} (${it.email})" }
        val adapter = DropdownAdapter(this, teacherNames)
        etTeacher.setAdapter(adapter)

        // Show dropdown when clicked
        etTeacher.setOnClickListener {
            etTeacher.showDropDown()
        }

        etTeacher.setOnItemClickListener { parent, _, position, _ ->
            selectedTeacherId = teachers[position].id
            teacherInputLayout.error = null // Clear error on selection
            loadSectionsForTeacherAndSubject()
        }
    }

    private fun setupTimeDropdowns() {
        val timeDisplayNames = timeSlots.map { it.displayName }
        val adapter = DropdownAdapter(this, timeDisplayNames)

        etTimeStart.setAdapter(adapter)
        etTimeEnd.setAdapter(adapter)

        // Show dropdown when clicked
        etTimeStart.setOnClickListener {
            etTimeStart.showDropDown()
        }

        etTimeEnd.setOnClickListener {
            etTimeEnd.showDropDown()
        }

        etTimeStart.setOnItemClickListener { parent, _, position, _ ->
            selectedTimeStartId = timeSlots[position].id
            timeStartInputLayout.error = null // Clear error on selection
        }

        etTimeEnd.setOnItemClickListener { parent, _, position, _ ->
            selectedTimeEndId = timeSlots[position].id
            timeEndInputLayout.error = null // Clear error on selection
        }
    }

    private fun setupSubjectDropdowns() {
        val subjectCodes = subjects.map { it.code }
        val subjectNames = subjects.map { it.name }

        val codeAdapter = DropdownAdapter(this, subjectCodes)
        val nameAdapter = DropdownAdapter(this, subjectNames)

        etSubjectCode.setAdapter(codeAdapter)
        etSubjectName.setAdapter(nameAdapter)

        // Show dropdown when clicked
        etSubjectCode.setOnClickListener {
            etSubjectCode.showDropDown()
        }

        etSubjectName.setOnClickListener {
            etSubjectName.showDropDown()
        }

        etSubjectCode.setOnItemClickListener { parent, _, position, _ ->
            selectedSubjectId = subjects[position].id
            etSubjectName.setText(subjects[position].name, false)
            subjectCodeInputLayout.error = null // Clear error on selection
            subjectNameInputLayout.error = null
            loadSectionsForTeacherAndSubject()
        }

        etSubjectName.setOnItemClickListener { parent, _, position, _ ->
            selectedSubjectId = subjects[position].id
            etSubjectCode.setText(subjects[position].code, false)
            subjectCodeInputLayout.error = null // Clear error on selection
            subjectNameInputLayout.error = null
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
        val adapter = DropdownAdapter(this, sectionNames)
        etSection.setAdapter(adapter)

        // Show dropdown when clicked
        etSection.setOnClickListener {
            etSection.showDropDown()
        }

        etSection.setOnItemClickListener { parent, _, position, _ ->
            selectedSectionId = sections[position].id
            sectionInputLayout.error = null // Clear error on selection
        }
    }

    private fun setupStatusDropdown() {
        val statuses = listOf("Pending", "Occupied", "Available")
        val adapter = DropdownAdapter(this, statuses)
        etStatus.setAdapter(adapter)

        // Show dropdown when clicked
        etStatus.setOnClickListener {
            etStatus.showDropDown()
        }

        etStatus.setOnItemClickListener { parent, _, position, _ ->
            statusInputLayout.error = null // Clear error on selection
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
            val teacherEmail = schedule.optString("teacher_email", "")
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

            // Set room if available
            val roomName = schedule.optString("room_name", "")
            if (roomName.isNotEmpty()) {
                val roomIndex = rooms.indexOfFirst { it.name.equals(roomName, ignoreCase = true) }
                if (roomIndex >= 0) {
                    etRoomName.setText(rooms[roomIndex].name, false)
                    selectedRoomId = rooms[roomIndex].id
                }
            }

            // Set status
            val status = schedule.optString("schedule_status", "Pending")
            etStatus.setText(status, false)

        } catch (e: Exception) {
            Toast.makeText(this, "Error populating form: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        // Validate Day
        if (etDay.text.toString().isEmpty() || selectedDayId == -1) {
            dayInputLayout.error = "Please select a day"
            isValid = false
        } else {
            dayInputLayout.error = null
        }

        // Validate Subject Code
        if (etSubjectCode.text.toString().isEmpty() || selectedSubjectId == -1) {
            subjectCodeInputLayout.error = "Please select a subject"
            isValid = false
        } else {
            subjectCodeInputLayout.error = null
        }

        // Validate Subject Name
        if (etSubjectName.text.toString().isEmpty()) {
            subjectNameInputLayout.error = "Please select a subject"
            isValid = false
        } else {
            subjectNameInputLayout.error = null
        }

        // Validate Section
        if (etSection.text.toString().isEmpty() || selectedSectionId == -1) {
            sectionInputLayout.error = "Please select a section"
            isValid = false
        } else {
            sectionInputLayout.error = null
        }

        // Validate Teacher
        if (etTeacher.text.toString().isEmpty() || selectedTeacherId == -1) {
            teacherInputLayout.error = "Please select a teacher"
            isValid = false
        } else {
            teacherInputLayout.error = null
        }

        // Validate Status
        if (etStatus.text.toString().isEmpty()) {
            statusInputLayout.error = "Please select a status"
            isValid = false
        } else {
            statusInputLayout.error = null
        }

        // Validate Time Start
        if (etTimeStart.text.toString().isEmpty() || selectedTimeStartId == -1) {
            timeStartInputLayout.error = "Please select start time"
            isValid = false
        } else {
            timeStartInputLayout.error = null
        }

        // Validate Time End
        if (etTimeEnd.text.toString().isEmpty() || selectedTimeEndId == -1) {
            timeEndInputLayout.error = "Please select end time"
            isValid = false
        } else {
            timeEndInputLayout.error = null
        }

        // Validate Room
        if (etRoomName.text.toString().isEmpty() || selectedRoomId == -1) {
            roomNameInputLayout.error = "Please select a room"
            isValid = false
        } else {
            roomNameInputLayout.error = null
        }

        return isValid
    }

    private fun saveSchedule() {
        val status = etStatus.text.toString()

        // Build JSON payload
        val payload = JSONObject().apply {
            put("day_id", selectedDayId)
            put("subject_id", selectedSubjectId)
            put("section_id", selectedSectionId)
            put("teacher_id", selectedTeacherId)
            put("time_start_id", selectedTimeStartId)
            put("time_end_id", selectedTimeEndId)
            put("room_id", selectedRoomId)
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