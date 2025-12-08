package com.example.schedulingSystem.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.schedulingSystem.R
import com.example.schedulingSystem.fragments.EditRoomDialogFragment
import com.example.schedulingSystem.models.RoomItem

class AdminRoomAdapter(
    private val activity: FragmentActivity,
    private val onRoomUpdated: () -> Unit
) : RecyclerView.Adapter<AdminRoomAdapter.RoomViewHolder>() {

    private var rooms = emptyList<RoomItem>()

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
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete1) // optional

        fun bind(room: RoomItem) {
            // Show room name + capacity
            val capacityText = itemView.context.getString(
                R.string.room_capacity_format,
                room.roomName,
                room.roomCapacity
            )
            tvRoomName.text = capacityText

            // EDIT BUTTON CLICK → Open dialog
            btnEdit.setOnClickListener {
                val dialog = EditRoomDialogFragment.newInstance(
                    roomId = room.roomId,
                    roomName = room.roomName,
                    roomCapacity = room.roomCapacity
                )

                // Refresh list after update
                dialog.setOnRoomUpdatedListener {
                    onRoomUpdated()
                }

                dialog.show(activity.supportFragmentManager, "EditRoomDialog")
            }

            // Optional: DELETE BUTTON (you can implement later)
            btnDelete.setOnClickListener {
                // TODO: Add delete confirmation + API call
                // showDeleteConfirmation(room)
            }
        }
    }
}