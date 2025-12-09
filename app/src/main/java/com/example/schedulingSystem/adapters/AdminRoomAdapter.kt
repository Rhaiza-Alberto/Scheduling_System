package com.example.schedulingSystem.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.schedulingSystem.R
import com.example.schedulingSystem.fragments.EditRoomDialogFragment
import com.example.schedulingSystem.models.RoomItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject


class AdminRoomAdapter(
    private val activity: FragmentActivity,
    private val onRoomUpdated: () -> Unit
) : RecyclerView.Adapter<AdminRoomAdapter.RoomViewHolder>() {

    private var rooms = emptyList<RoomItem>()

    companion object {
        private const val BACKEND_URL = "http://10.0.2.2/scheduling-api"
    }

    fun submitList(newRooms: List<RoomItem>) {
        rooms = newRooms
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_edit_room, parent, false)
        return RoomViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        holder.bind(rooms[position])
    }

    override fun getItemCount() = rooms.size

    inner class RoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvRoomName: TextView = itemView.findViewById(R.id.tvRoomName)
        private val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit1)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete1)

        fun bind(room: RoomItem) {
            tvRoomName.text = itemView.context.getString(
                R.string.room_capacity_format,
                room.roomName,
                room.roomCapacity
            )

            // EDIT
            btnEdit.setOnClickListener {
                val dialog = EditRoomDialogFragment.newInstance(
                    roomId = room.roomId,
                    roomName = room.roomName,
                    roomCapacity = room.roomCapacity
                )
                dialog.setOnRoomUpdatedListener { onRoomUpdated() }
                dialog.show(activity.supportFragmentManager, "EditRoom")
            }

            // DELETE (Soft Delete)
            btnDelete.setOnClickListener {
                androidx.appcompat.app.AlertDialog.Builder(itemView.context)
                    .setTitle("Remove Room")
                    .setMessage("Remove \"${room.roomName}\" from the list?\n\nYou can restore it later.")
                    .setPositiveButton("Remove") { _, _ ->
                        softDeleteRoom(room.roomId)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }

        private fun softDeleteRoom(roomId: Int) {
            val json = JSONObject().apply { put("room_id", roomId) }
            val body = json.toString().toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("$BACKEND_URL/soft_delete_room.php")
                .post(body)
                .build()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = OkHttpClient().newCall(request).execute()
                    val res = response.body?.string() ?: ""

                    withContext(Dispatchers.Main) {
                        val result = JSONObject(res)
                        Toast.makeText(
                            itemView.context,
                            result.getString("message"),
                            Toast.LENGTH_SHORT
                        ).show()

                        if (result.getBoolean("success")) {
                            onRoomUpdated() // Refresh list
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(itemView.context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }


}