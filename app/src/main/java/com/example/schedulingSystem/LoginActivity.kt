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
import java.util.concurrent.TimeUnit // Import the necessary TimeUnit

class LoginActivity : AppCompatActivity() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS) // Set connection timeout
        .readTimeout(10, TimeUnit.SECONDS)    // Set read timeout
        .build()

    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnSignIn: MaterialButton

    companion object {
        // UPDATE THIS IP ADDRESS TO YOUR PC IP (from ipconfig)
        // Example: "192.168.1.100" or "10.0.2.2"
        private const val BACKEND_URL = "http://10.0.2.2/scheduling-api"
        
        // If 10.0.2.2 doesn't work, try your PC IP instead:
        // private const val BACKEND_URL = "http://192.168.1.103/scheduling-api"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Find views manually
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

        // Test connectivity on app start
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
                Log.e("LoginActivity", "✗ Exception type: ${e.javaClass.simpleName}")
                
                // Log the root cause
                var cause = e.cause
                var depth = 0
                while (cause != null && depth < 3) {
                    Log.e("LoginActivity", "✗ Caused by (${depth + 1}): ${cause.message}")
                    cause = cause.cause
                    depth++
                }
                
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

                        getSharedPreferences("user_session", MODE_PRIVATE).edit {
                            putInt("user_id", user.getInt("person_ID"))
                            putString("username", user.getString("username"))
                            putString("full_name", user.getString("name"))
                            putString("account_type", user.getString("account_type"))
                            putBoolean("is_logged_in", true)
                        }

                        Toast.makeText(this@LoginActivity, "Welcome, ${user.getString("name")}!", Toast.LENGTH_LONG).show()

                        val intent = when (user.getString("account_type").lowercase()) {
                            "admin" -> Intent(this@LoginActivity, AdminDashboardActivity::class.java)
                            else -> Intent(this@LoginActivity, TeacherDashboardActivity::class.java)
                        }
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, jsonResponse.optString("message", "Login failed"), Toast.LENGTH_LONG).show()
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