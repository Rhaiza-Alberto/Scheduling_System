package com.example.schedulingSystem

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import androidx.core.content.edit
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnSignIn: MaterialButton

    companion object {
        // Change this to your PC's IP address if needed
        private const val BACKEND_URL = "http://10.0.2.2/scheduling-api"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnSignIn = findViewById(R.id.btnSignIn)

        btnSignIn.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            performLogin(email, password)
        }

        testConnectivity()
    }

    private fun testConnectivity() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("LoginActivity", "→ Testing connectivity to $BACKEND_URL/test.php")
                val request = Request.Builder()
                    .url("$BACKEND_URL/test.php")
                    .get()
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""
                Log.d("LoginActivity", "✓ Test Connection - Code: ${response.code}")
                Log.d("LoginActivity", "✓ Response: $responseBody")

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LoginActivity, "✓ Backend connected!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Unknown error"
                Log.e("LoginActivity", "✗ Test Connection Failed: $errorMsg")

                withContext(Dispatchers.Main) {
                    val displayMsg = when {
                        errorMsg.contains("ENETUNREACH") -> "Network unreachable - Check emulator network settings"
                        errorMsg.contains("Connection refused") -> "Connection refused - XAMPP not running?"
                        errorMsg.contains("Network is unreachable") -> "Network unreachable - Emulator can't reach host"
                        else -> "Backend unreachable: $errorMsg"
                    }
                    Toast.makeText(this@LoginActivity, displayMsg, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun performLogin(email: String, password: String) {
        val json = """
            {
                "username": "$email",
                "password": "$password"
            }
        """.trimIndent()

        val body = json.toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url("$BACKEND_URL/login.php")
            .post(body)
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("LoginActivity", "→ Sending login request for: $email")
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""

                Log.d("LoginActivity", "← Response Code: ${response.code}")
                Log.d("LoginActivity", "← Response Body: $responseBody")

                if (responseBody.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@LoginActivity, "Empty response from server", Toast.LENGTH_LONG).show()
                    }
                    return@launch
                }

                val jsonResponse = JSONObject(responseBody)
                val success = jsonResponse.getBoolean("success")

                withContext(Dispatchers.Main) {
                    if (success) {
                        val user = jsonResponse.getJSONObject("user")

                        // Get all user data
                        val personId = user.getInt("person_ID")
                        val username = user.getString("username")
                        val fullName = user.getString("name")
                        val accountType = user.getString("account_type")
                        val accountId = user.getInt("account_ID")

                        // Get teacher_ID if present
                        val teacherId = if (user.has("teacher_ID")) {
                            user.getInt("teacher_ID")
                        } else {
                            -1
                        }

                        // Save to SharedPreferences
                        getSharedPreferences("user_session", MODE_PRIVATE).edit {
                            putInt("user_id", personId)
                            putString("username", username)
                            putString("full_name", fullName)
                            putString("account_type", accountType)
                            putInt("account_id", accountId)
                            putInt("teacher_id", teacherId)
                            putBoolean("is_logged_in", true)
                        }

                        Log.d("LoginActivity", "✓ Login successful - Account Type: $accountType, Teacher ID: $teacherId")
                        Toast.makeText(this@LoginActivity, "Welcome, $fullName!", Toast.LENGTH_SHORT).show()

                        // Navigate based on account type
                        val intent = when (accountType.lowercase()) {
                            "admin" -> {
                                Log.d("LoginActivity", "→ Navigating to Admin Dashboard")
                                Intent(this@LoginActivity, AdminDashboardActivity::class.java)
                            }
                            "teacher" -> {
                                Log.d("LoginActivity", "→ Navigating to Teacher Dashboard")
                                Intent(this@LoginActivity, TeacherDashboardActivity::class.java)
                            }
                            else -> {
                                Log.w("LoginActivity", "Unknown account type: $accountType, defaulting to Teacher Dashboard")
                                Intent(this@LoginActivity, TeacherDashboardActivity::class.java)
                            }
                        }

                        startActivity(intent)
                        finish()
                    } else {
                        val message = jsonResponse.optString("message", "Login failed")
                        Toast.makeText(this@LoginActivity, message, Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("LoginActivity", "✗ Connection error: ${e.message}", e)
                    Toast.makeText(this@LoginActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}