package com.example.schedulingSystem.services

import com.example.schedulingSystem.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class DropdownDataService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val BACKEND_URL = "http://10.0.2.2/scheduling-api"
    }

    suspend fun getDays(): Result<List<Day>> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$BACKEND_URL/get_days.php")
                .build()
            
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            
            val json = JSONObject(body)
            if (!json.getBoolean("success")) {
                return@withContext Result.failure(Exception("Failed to fetch days"))
            }
            
            val daysArray = json.getJSONArray("days")
            val days = mutableListOf<Day>()
            
            for (i in 0 until daysArray.length()) {
                val dayJson = daysArray.getJSONObject(i)
                days.add(
                    Day(
                        id = dayJson.getInt("id"),
                        name = dayJson.getString("name")
                    )
                )
            }
            
            Result.success(days)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTeachers(): Result<List<Teacher>> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$BACKEND_URL/get_teachers.php")
                .build()
            
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            
            val json = JSONObject(body)
            if (!json.getBoolean("success")) {
                return@withContext Result.failure(Exception("Failed to fetch teachers"))
            }
            
            val teachersArray = json.getJSONArray("teachers")
            val teachers = mutableListOf<Teacher>()
            
            for (i in 0 until teachersArray.length()) {
                val teacherJson = teachersArray.getJSONObject(i)
                teachers.add(
                    Teacher(
                        id = teacherJson.getInt("id"),
                        name = teacherJson.getString("name"),
                        email = teacherJson.optString("email", "")
                    )
                )
            }
            
            Result.success(teachers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTimeSlots(): Result<List<TimeSlot>> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$BACKEND_URL/get_time_slots.php")
                .build()
            
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            
            val json = JSONObject(body)
            if (!json.getBoolean("success")) {
                return@withContext Result.failure(Exception("Failed to fetch time slots"))
            }
            
            val timesArray = json.getJSONArray("times")
            val timeSlots = mutableListOf<TimeSlot>()
            
            for (i in 0 until timesArray.length()) {
                val timeJson = timesArray.getJSONObject(i)
                timeSlots.add(
                    TimeSlot(
                        id = timeJson.getInt("id"),
                        displayName = timeJson.getString("display_name"),
                        startTime = timeJson.getString("start_time"),
                        endTime = timeJson.getString("end_time")
                    )
                )
            }
            
            Result.success(timeSlots)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSubjects(): Result<List<Subject>> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$BACKEND_URL/get_subjects.php")
                .build()
            
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            
            val json = JSONObject(body)
            if (!json.getBoolean("success")) {
                return@withContext Result.failure(Exception("Failed to fetch subjects"))
            }
            
            val subjectsArray = json.getJSONArray("subjects")
            val subjects = mutableListOf<Subject>()
            
            for (i in 0 until subjectsArray.length()) {
                val subjectJson = subjectsArray.getJSONObject(i)
                subjects.add(
                    Subject(
                        id = subjectJson.getInt("id"),
                        code = subjectJson.getString("code"),
                        name = subjectJson.getString("name")
                    )
                )
            }
            
            Result.success(subjects)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSections(teacherId: Int? = null, subjectId: Int? = null): Result<List<Section>> = withContext(Dispatchers.IO) {
        try {
            val url = if (teacherId != null && subjectId != null) {
                "$BACKEND_URL/get_sections.php?teacher_id=$teacherId&subject_id=$subjectId"
            } else if (teacherId != null) {
                "$BACKEND_URL/get_sections.php?teacher_id=$teacherId"
            } else if (subjectId != null) {
                "$BACKEND_URL/get_sections.php?subject_id=$subjectId"
            } else {
                "$BACKEND_URL/get_sections.php"
            }
            
            val request = Request.Builder()
                .url(url)
                .build()
            
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            
            val json = JSONObject(body)
            if (!json.getBoolean("success")) {
                return@withContext Result.failure(Exception("Failed to fetch sections"))
            }
            
            val sectionsArray = json.getJSONArray("sections")
            val sections = mutableListOf<Section>()
            
            for (i in 0 until sectionsArray.length()) {
                val sectionJson = sectionsArray.getJSONObject(i)
                sections.add(
                    Section(
                        id = sectionJson.getInt("id"),
                        name = sectionJson.getString("name"),
                        year = sectionJson.getInt("year"),
                        teacherId = sectionJson.getInt("teacher_id"),
                        subjectId = sectionJson.getInt("subject_id")
                    )
                )
            }
            
            Result.success(sections)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRooms(): Result<List<Room>> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$BACKEND_URL/get_rooms.php")
                .build()
            
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            
            val json = JSONObject(body)
            if (!json.getBoolean("success")) {
                return@withContext Result.failure(Exception("Failed to fetch rooms"))
            }
            
            val roomsArray = json.getJSONArray("rooms")
            val rooms = mutableListOf<Room>()
            
            for (i in 0 until roomsArray.length()) {
                val roomJson = roomsArray.getJSONObject(i)
                rooms.add(
                    Room(
                        id = roomJson.getInt("id"),
                        name = roomJson.getString("name"),
                        capacity = roomJson.getInt("capacity")
                    )
                )
            }
            
            Result.success(rooms)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
