package com.example.schedulingSystem.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.schedulingSystem.R
import com.example.schedulingSystem.databinding.DialogAdminEditRoomBinding
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

class EditRoomDialogFragment : DialogFragment() {

    private lateinit var binding: DialogAdminEditRoomBinding
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val BACKEND_URL = "http://10.0.2.2/scheduling-api"
        private const val ARG_ROOM_ID = "room_id"
        private const val ARG_ROOM_NAME = "room_name"
        private const val ARG_ROOM_CAPACITY = "room_capacity"

        fun newInstance(roomId: Int, roomName: String, roomCapacity: Int): EditRoomDialogFragment {
            return EditRoomDialogFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_ROOM_ID, roomId)
                    putString(ARG_ROOM_NAME, roomName)
                    putInt(ARG_ROOM_CAPACITY, roomCapacity)
                }
            }
        }
    }

    private var roomId: Int = -1
    private var originalName: String = ""
    private var originalCapacity: Int = 0
    private var onRoomUpdated: (() -> Unit)? = null

    fun setOnRoomUpdatedListener(listener: () -> Unit) {
        onRoomUpdated = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_SchedulingSystem)

        // Get data from arguments
        arguments?.let {
            roomId = it.getInt(ARG_ROOM_ID, -1)
            originalName = it.getString(ARG_ROOM_NAME, "")
            originalCapacity = it.getInt(ARG_ROOM_CAPACITY, 0)
        }

        if (roomId == -1 || originalName.isEmpty()) {
            Toast.makeText(requireContext(), "Error loading room", Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogAdminEditRoomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Pre-fill fields
        binding.etEditRoomName.setText(originalName)
        binding.etEditCapacity.setText(originalCapacity.toString())

        // Setup button listeners
        binding.btnCancelEdit.setOnClickListener {
            dismiss()
        }

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
                Toast.makeText(requireContext(), "No changes made", Toast.LENGTH_SHORT).show()
                dismiss()
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
                            Toast.makeText(
                                requireContext(),
                                "Room updated successfully!",
                                Toast.LENGTH_SHORT
                            ).show()
                            onRoomUpdated?.invoke()
                            dismiss()
                        } else {
                            Toast.makeText(
                                requireContext(),
                                result.getString("message"),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Server error: ${response.code}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "Network error: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
    }
}