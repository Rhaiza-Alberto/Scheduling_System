package com.example.schedulingSystem.services

import com.example.schedulingSystem.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

/**
 * Service class for fetching data for dropdown menus from the backend API.
 */
class DropdownDataService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val BACKEND_URL = "http://10.0.2.2/scheduling-api"
    }

    /**
     * Fetches a list of days.
     * @return A [Result] containing a list of [Day] objects on success, or an exception on failure.
     */
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

    /**
     * Fetches a list of teachers.
     * @return A [Result] containing a list of [Teacher] objects on success, or an exception on failure.
     */
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
                        name = teacherJson.getString("teacher_name"),
                        email = teacherJson.optString("email", "")
                    )
                )
            }
            
            Result.success(teachers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetches a list of time slots.
     * @return A [Result] containing a list of [TimeSlot] objects on success, or an exception on failure.
     */
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

    /**
     * Fetches a list of subjects.
     * @return A [Result] containing a list of [Subject] objects on success, or an exception on failure.
     */
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

    /**
     * Fetches a list of sections, optionally filtered by teacher and/or subject.
     * @param teacherId The ID of the teacher to filter by.
     * @param subjectId The ID of the subject to filter by.
     * @return A [Result] containing a list of [Section] objects on success, or an exception on failure.
     */
    suspend fun getSections(teacherId: Int? = null, subjectId: Int? = null): Result<List<Section>> = withContext(Dispatchers.IO) {
        try {
            // Build URL with query parameters
            val urlBuilder = "$BACKEND_URL/get_sections.php".toHttpUrlOrNull()?.newBuilder()
                ?: return@withContext Result.failure(Exception("Invalid URL"))
            
            if (teacherId != null) {
                urlBuilder.addQueryParameter("teacher_id", teacherId.toString())
            }
            if (subjectId != null) {
                urlBuilder.addQueryParameter("subject_id", subjectId.toString())
            }
            
            val url = urlBuilder.build().toString()
            
            val request = Request.Builder()
                .url(url)
                .build()
                
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            
            val json = JSONObject(body)
            if (!json.getBoolean("success")) {
                val errorMsg = json.optString("message", "Failed to fetch sections")
                return@withContext Result.failure(Exception(errorMsg))
            }
            
            val sectionsArray = json.getJSONArray("sections")
            val sections = mutableListOf<Section>()
            
            for (i in 0 until sectionsArray.length()) {
                val sectionJson = sectionsArray.getJSONObject(i)
                sections.add(
                    Section(
                        id = sectionJson.getInt("id"),
                        name = sectionJson.getString("section_name"),
                        year = sectionJson.getInt("section_year")
                    )
                )
            }
            
            Result.success(sections)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetches a list of rooms.
     * @return A [Result] containing a list of [Room] objects on success, or an exception on failure.
     */
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
