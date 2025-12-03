package com.example.schedulingSystem.network

class ApiService {
}

package com.example.schedulingSystem.network

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/**
 * API Service for handling network requests
 * Change BASE_URL to your actual server address
 */
object ApiService {
    // For Android Emulator: use 10.0.2.2 to access localhost
    // For Physical Device: use your computer's IP address (e.g., "http://192.168.1.100")
    private const val BASE_URL = "http://10.0.2.2/scheduling-api"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    /**
     * Login request
     */
    suspend fun login(username: String, password: String): ApiResponse<LoginResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val requestBody = mapOf(
                    "username" to username,
                    "password" to password
                )

                val body = gson.toJson(requestBody).toRequestBody(JSON_MEDIA_TYPE)

                val request = Request.Builder()
                    .url("$BASE_URL/login.php")
                    .post(body)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (response.isSuccessful && responseBody != null) {
                    val loginResponse = gson.fromJson(responseBody, LoginResponse::class.java)
                    if (loginResponse.success) {
                        ApiResponse.Success(loginResponse)
                    } else {
                        ApiResponse.Error(loginResponse.message ?: "Login failed")
                    }
                } else {
                    ApiResponse.Error("Server error: ${response.code}")
                }
            } catch (e: Exception) {
                ApiResponse.Error("Network error: ${e.message}")
            }
        }
    }

    /**
     * Get account types for registration
     */
    suspend fun getAccountTypes(): ApiResponse<AccountTypesResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("$BASE_URL/get_account_types.php")
                    .get()
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (response.isSuccessful && responseBody != null) {
                    val accountTypesResponse = gson.fromJson(responseBody, AccountTypesResponse::class.java)
                    if (accountTypesResponse.success) {
                        ApiResponse.Success(accountTypesResponse)
                    } else {
                        ApiResponse.Error(accountTypesResponse.message ?: "Failed to get account types")
                    }
                } else {
                    ApiResponse.Error("Server error: ${response.code}")
                }
            } catch (e: Exception) {
                ApiResponse.Error("Network error: ${e.message}")
            }
        }
    }

    /**
     * Verify token (check if user is still valid)
     */
    suspend fun verifyToken(personId: Int): ApiResponse<LoginResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val requestBody = mapOf("person_ID" to personId)
                val body = gson.toJson(requestBody).toRequestBody(JSON_MEDIA_TYPE)

                val request = Request.Builder()
                    .url("$BASE_URL/verify_token.php")
                    .post(body)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (response.isSuccessful && responseBody != null) {
                    val verifyResponse = gson.fromJson(responseBody, LoginResponse::class.java)
                    if (verifyResponse.success) {
                        ApiResponse.Success(verifyResponse)
                    } else {
                        ApiResponse.Error(verifyResponse.message ?: "Token verification failed")
                    }
                } else {
                    ApiResponse.Error("Server error: ${response.code}")
                }
            } catch (e: Exception) {
                ApiResponse.Error("Network error: ${e.message}")
            }
        }
    }

    /**
     * Get today's schedule
     */
    suspend fun getTodaySchedule(): ApiResponse<List<ScheduleItem>> {
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("$BASE_URL/get-schedule.php")
                    .get()
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (response.isSuccessful && responseBody != null) {
                    val schedules = gson.fromJson(responseBody, Array<ScheduleItem>::class.java).toList()
                    ApiResponse.Success(schedules)
                } else {
                    ApiResponse.Error("Server error: ${response.code}")
                }
            } catch (e: Exception) {
                ApiResponse.Error("Network error: ${e.message}")
            }
        }
    }
}

// Response wrapper
sealed class ApiResponse<out T> {
    data class Success<T>(val data: T) : ApiResponse<T>()
    data class Error(val message: String) : ApiResponse<Nothing>()
}

// Data classes for API responses
data class LoginResponse(
    val success: Boolean,
    val message: String?,
    val user: User?
)

data class User(
    val person_ID: Int,
    val username: String,
    val account_type: String,
    val account_ID: Int,
    val name: String
)

data class AccountTypesResponse(
    val success: Boolean,
    val message: String?,
    val account_types: List<AccountType>?
)

data class AccountType(
    val account_ID: Int,
    val account_name: String
)

// ScheduleItem is already defined in Schedule.kt