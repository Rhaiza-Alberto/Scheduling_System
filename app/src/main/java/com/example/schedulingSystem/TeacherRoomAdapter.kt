package com.example.schedulingSystem

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class TeacherRoomAdapter : ListAdapter<RoomAvailability, TeacherRoomAdapter.ViewHolder>(RoomDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_teacher_if_room_available, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvRoomName: TextView = itemView.findViewById(R.id.tvRoomName)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)

        fun bind(room: RoomAvailability) {
            tvRoomName.text = room.roomName

            // Update status text and color based on availability
            if (room.isAvailable) {
                tvStatus.text = "Available • Capacity: ${room.roomCapacity}"
                tvStatus.setTextColor(
                    itemView.context.getColor(R.color.primary_dark_green)
                )
            } else {
                tvStatus.text = room.status
                tvStatus.setTextColor(
                    itemView.context.getColor(R.color.status_red_text)
                )
            }
        }
    }
}

class RoomDiffCallback : DiffUtil.ItemCallback<RoomAvailability>() {
    override fun areItemsTheSame(oldItem: RoomAvailability, newItem: RoomAvailability): Boolean {
        return oldItem.roomId == newItem.roomId
    }

    override fun areContentsTheSame(oldItem: RoomAvailability, newItem: RoomAvailability): Boolean {
        return oldItem == newItem
    }
}