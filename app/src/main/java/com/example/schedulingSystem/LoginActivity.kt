package com.example.schedulingSystem

import android.content.Intent
import android.os.Bundle
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

class LoginActivity : AppCompatActivity() {

    private val client = OkHttpClient()

    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnSignIn: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)  // ← This works!

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
    }

    private fun performLogin(email: String, password: String) {
        val json = """
            {
                "email": "$email",
                "password": "$password"
            }
        """.trimIndent()

        val body = json.toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url("http://10.0.2.2/scheduling-api/login.php")
            .post(body)
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""
                val jsonResponse = JSONObject(responseBody)
                val success = jsonResponse.getBoolean("success")

                withContext(Dispatchers.Main) {
                    if (success) {
                        val user = jsonResponse.getJSONObject("user")

                        getSharedPreferences("user_session", MODE_PRIVATE).edit {
                            putInt("user_id", user.getInt("id"))
                            putString("username", user.getString("username"))
                            putString("first_name", user.getString("first_name"))
                            putString("last_name", user.getString("last_name"))
                            putString("account_type", user.getString("account_type"))
                            putBoolean("is_logged_in", true)
                        }

                        Toast.makeText(this@LoginActivity, "Welcome, ${user.getString("first_name")}!", Toast.LENGTH_LONG).show()

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
                    Toast.makeText(this@LoginActivity, "Connection failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}