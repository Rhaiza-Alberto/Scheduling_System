package com.example.schedulingSystem

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

// Define the Room data class here since it's being used
data class RoomItem(
    val id: Int,
    val name: String,
    val capacity: Int,
    val status: String,
    val isAvailable: Boolean
)

class TeacherRoomAdapter : ListAdapter<RoomItem, TeacherRoomAdapter.ViewHolder>(RoomDiffCallback()) {

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

        fun bind(room: RoomItem) {
            tvRoomName.text = room.name
            tvStatus.text = room.status
            tvStatus.setTextColor(
                itemView.context.getColor(
                    if (room.isAvailable) R.color.primary_dark_green
                    else R.color.status_red_text
                )
            )
        }
    }
}

class RoomDiffCallback : DiffUtil.ItemCallback<RoomItem>() {
    override fun areItemsTheSame(oldItem: RoomItem, newItem: RoomItem) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: RoomItem, newItem: RoomItem) = oldItem == newItem
}