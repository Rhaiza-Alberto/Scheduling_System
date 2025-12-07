package com.example.schedulingSystem

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnSignIn: MaterialButton

    companion object {
        private const val BACKEND_URL = "http://10.0.2.2/scheduling-api" // Emulator → localhost
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
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            performLogin(email, password)
        }

        testConnectivity()
    }

    private fun testConnectivity() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = Request.Builder()
                    .url("$BACKEND_URL/test.php")
                    .get()
                    .build()

                client.newCall(request).execute().use { response ->
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@LoginActivity, "Backend Connected!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                // Silent – no toast spam on startup
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
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""

                Log.d("LoginActivity", "Raw response: $responseBody")

                if (!response.isSuccessful || responseBody.isBlank()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@LoginActivity, "Server error or empty response", Toast.LENGTH_LONG).show()
                    }
                    return@launch
                }

                val jsonResponse = JSONObject(responseBody)
                val success = jsonResponse.getBoolean("success")

                withContext(Dispatchers.Main) {
                    if (success) {
                        val user = jsonResponse.getJSONObject("user")

                        val personId    = user.getInt("person_ID")
                        val username    = user.getString("username")
                        val fullName    = user.getString("name")
                        val accountType = user.getString("account_type")   // "Admin" or "Teacher"
                        val accountId   = user.getInt("account_ID")

                        // Save session
                        getSharedPreferences("user_session", MODE_PRIVATE).edit {
                            putInt("user_id", personId)
                            putString("username", username)
                            putString("full_name", fullName)
                            putString("account_type", accountType)
                            putInt("account_id", accountId)
                            putBoolean("is_logged_in", true)
                            apply()
                        }

                        Toast.makeText(this@LoginActivity, "Welcome, $fullName!", Toast.LENGTH_SHORT).show()

                        val intent = when (accountType.uppercase()) {
                            "ADMIN"  -> Intent(this@LoginActivity, AdminDashboardActivity::class.java)
                            "TEACHER"-> Intent(this@LoginActivity, TeacherDashboardActivity::class.java)
                            else     -> Intent(this@LoginActivity, TeacherDashboardActivity::class.java)
                        }

                        startActivity(intent)
                        finish()

                    } else {
                        val msg = jsonResponse.optString("message", "Invalid credentials")
                        Toast.makeText(this@LoginActivity, msg, Toast.LENGTH_LONG).show()
                    }
                }

            } catch (e: Exception) {
                Log.e("LoginActivity", "Exception", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LoginActivity, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}