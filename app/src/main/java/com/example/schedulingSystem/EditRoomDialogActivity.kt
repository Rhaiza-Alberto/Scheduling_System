package com.example.schedulingSystem

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.schedulingSystem.databinding.DialogAdminEditRoomBinding
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class EditRoomDialogActivity : AppCompatActivity() {

    private lateinit var binding: DialogAdminEditRoomBinding

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val BACKEND_URL = "http://10.0.2.2/scheduling-api"
    }

    private var roomId: Int = -1
    private var originalName: String = ""
    private var originalCapacity: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get data from intent
        roomId = intent.getIntExtra("room_id", -1)
        originalName = intent.getStringExtra("room_name") ?: ""
        originalCapacity = intent.getIntExtra("room_capacity", 0)

        if (roomId == -1 || originalName.isEmpty()) {
            Toast.makeText(this, "Error loading room", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Use ViewBinding
        binding = DialogAdminEditRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Pre-fill fields
        binding.etEditRoomName.setText(originalName)
        binding.etEditCapacity.setText(originalCapacity.toString())

        binding.btnCancelEdit.setOnClickListener { finish() }

        binding.btnUpdateRoom.setOnClickListener {
            val newName = binding.etEditRoomName.text.toString().trim()
            val newCapacityText = binding.etEditCapacity.text.toString()

            if (newName.isEmpty()) {
                binding.etEditRoomName.error = "Room name required"
                return@setOnClickListener
            }
            if (newCapacityText.isEmpty() || newCapacityText.toIntOrNull() == null || newCapacityText.toInt() <= 0) {
                binding.etEditCapacity.error = "Valid capacity required"
                return@setOnClickListener
            }

            val newCapacity = newCapacityText.toInt()

            if (newName == originalName && newCapacity == originalCapacity) {
                Toast.makeText(this, "No changes made", Toast.LENGTH_SHORT).show()
                finish()
                return@setOnClickListener
            }

            updateRoom(roomId, newName, newCapacity)
        }
    }

    private fun updateRoom(roomId: Int, name: String, capacity: Int) {
        val json = JSONObject().apply {
            put("room_id", roomId)
            put("room_name", name)
            put("room_capacity", capacity)
        }

        val body = json.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("$BACKEND_URL/update_room.php")
            .post(body)
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = client.newCall(request).execute()
                val res = response.body?.string() ?: ""

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val result = JSONObject(res)
                        if (result.getBoolean("success")) {
                            Toast.makeText(this@EditRoomDialogActivity, "Room updated successfully!", Toast.LENGTH_SHORT).show()
                            setResult(Activity.RESULT_OK)
                            finish()
                        } else {
                            Toast.makeText(this@EditRoomDialogActivity, result.getString("message"), Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(this@EditRoomDialogActivity, "Server error: ${response.code}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EditRoomDialogActivity, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}