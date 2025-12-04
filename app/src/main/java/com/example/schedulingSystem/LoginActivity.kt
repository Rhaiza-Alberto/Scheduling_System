package com.example.schedulingSystem

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "LoginActivity"
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnSignIn: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnSignIn = findViewById(R.id.btnSignIn)

        btnSignIn.setOnClickListener {
            val username = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            login(username, password)
        }
    }

    private fun login(username: String, password: String) {
        btnSignIn.isEnabled = false
        btnSignIn.text = "Logging in..."

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Create JSON body
                val json = JSONObject()
                json.put("username", username)
                json.put("password", password)

                val body = json.toString().toRequestBody("application/json".toMediaType())

                // Create request
                val request = Request.Builder()
                    .url("http://10.0.2.2/scheduling-api/login.php")
                    .post(body)
                    .build()

                Log.d(TAG, "════════════════════════════════════")
                Log.d(TAG, "LOGIN REQUEST")
                Log.d(TAG, "URL: http://10.0.2.2/scheduling-api/login.php")
                Log.d(TAG, "Body: $json")
                Log.d(TAG, "════════════════════════════════════")

                // Execute request
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""

                Log.d(TAG, "════════════════════════════════════")
                Log.d(TAG, "LOGIN RESPONSE")
                Log.d(TAG, "Status Code: ${response.code}")
                Log.d(TAG, "Response Length: ${responseBody.length} characters")
                Log.d(TAG, "Response Body:")
                Log.d(TAG, responseBody)
                Log.d(TAG, "════════════════════════════════════")

                withContext(Dispatchers.Main) {
                    handleResponse(responseBody)
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "Connection error: ${e.message}", e)
                    showError("Connection Error", "Could not connect to server:\n\n${e.message}")
                    resetButton()
                }
            }
        }
    }

    private fun handleResponse(responseBody: String) {
        try {
            // Check if response is empty
            if (responseBody.isEmpty()) {
                showError("Empty Response", "Server returned an empty response")
                resetButton()
                return
            }

            // Check if response starts with HTML (common error)
            if (responseBody.trim().startsWith("<")) {
                showError(
                    "HTML Response Received",
                    "Server returned HTML instead of JSON.\n\n" +
                            "This usually means:\n" +
                            "1. PHP error occurred\n" +
                            "2. Wrong URL\n" +
                            "3. File not found\n\n" +
                            "First 200 characters:\n${responseBody.take(200)}"
                )
                resetButton()
                return
            }

            // Try to parse JSON
            val jsonResponse = try {
                JSONObject(responseBody)
            } catch (e: JSONException) {
                Log.e(TAG, "JSON Parse Error: ${e.message}")
                showError(
                    "Invalid JSON",
                    "Cannot parse server response as JSON:\n\n" +
                            "Error: ${e.message}\n\n" +
                            "Response:\n${responseBody.take(500)}"
                )
                resetButton()
                return
            }

            Log.d(TAG, "JSON parsed successfully")
            Log.d(TAG, "JSON keys: ${jsonResponse.keys().asSequence().toList()}")

            // Check for success field
            val success = jsonResponse.optBoolean("success", false)

            Log.d(TAG, "Success field: $success")

            if (success) {
                handleLoginSuccess(jsonResponse)
            } else {
                val message = jsonResponse.optString("message", "Login failed")
                Log.e(TAG, "Login failed: $message")
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                resetButton()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error handling response", e)
            showError("Processing Error", "Error processing response:\n\n${e.message}")
            resetButton()
        }
    }

    private fun handleLoginSuccess(jsonResponse: JSONObject) {
        try {
            val user = jsonResponse.getJSONObject("user")

            val personId = user.getInt("person_ID")
            val userName = user.getString("username")
            val accountType = user.getString("account_type")
            val name = user.getString("name")

            Log.d(TAG, "Login successful!")
            Log.d(TAG, "User ID: $personId")
            Log.d(TAG, "Username: $userName")
            Log.d(TAG, "Account Type: $accountType")
            Log.d(TAG, "Name: $name")

            // Parse name for first/last name
            val nameParts = name.split(" ").filter { it.isNotBlank() }
            val firstName = nameParts.firstOrNull() ?: "User"
            val lastName = nameParts.lastOrNull() ?: ""

            // Save to preferences
            getSharedPreferences("user_session", MODE_PRIVATE).edit {
                putInt("user_id", personId)
                putString("username", userName)
                putString("full_name", name)
                putString("first_name", firstName)
                putString("last_name", lastName)
                putString("account_type", accountType)
                putBoolean("is_logged_in", true)
                putLong("login_time", System.currentTimeMillis())
            }

            Toast.makeText(this, "Welcome, $name!", Toast.LENGTH_SHORT).show()

            // Navigate based on account type
            val intent = when (accountType.lowercase()) {
                "admin" -> {
                    Log.d(TAG, "Navigating to Admin Dashboard")
                    Intent(this, AdminDashboardActivity::class.java)
                }
                else -> {
                    Log.d(TAG, "Navigating to Teacher Dashboard")
                    Intent(this, TeacherDashboardActivity::class.java)
                }
            }

            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()

        } catch (e: Exception) {
            Log.e(TAG, "Error processing user data", e)
            showError("Data Error", "Error reading user information:\n\n${e.message}")
            resetButton()
        }
    }

    private fun showError(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun resetButton() {
        btnSignIn.isEnabled = true
        btnSignIn.text = "Sign In"
    }
}