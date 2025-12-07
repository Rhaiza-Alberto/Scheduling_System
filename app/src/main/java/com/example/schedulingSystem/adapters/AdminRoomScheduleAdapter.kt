package com.example.schedulingSystem.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.schedulingSystem.R
import com.example.schedulingSystem.models.RoomItem

class AdminRoomScheduleAdapter(
    private val onRoomClick: ((RoomItem) -> Unit)? = null
) : RecyclerView.Adapter<AdminRoomScheduleAdapter.RoomViewHolder>() {

    private var rooms = emptyList<RoomItem>()

    fun submitList(newRooms: List<RoomItem>) {
        rooms = newRooms
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_room, parent, false)
        return RoomViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        holder.bind(rooms[position])
    }

    override fun getItemCount() = rooms.size

    inner class RoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvRoomName: TextView = itemView.findViewById(R.id.tvRoomName)

        fun bind(room: RoomItem) {
            // Only show: "Room Name • 30 seats" - clean and translatable
            val capacityText = itemView.context.getString(
                R.string.room_capacity_format,
                room.roomName,
                room.roomCapacity
            )
            tvRoomName.text = capacityText

            // Add click listener
            itemView.setOnClickListener {
                onRoomClick?.invoke(room)
            }
        }
    }
}