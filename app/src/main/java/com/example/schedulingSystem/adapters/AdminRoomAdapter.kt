package com.example.schedulingSystem.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.schedulingSystem.fragments.EditRoomDialogFragment
import com.example.schedulingSystem.R
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

        fun bind(room: RoomItem) {
            // Only show: "Room Name • 30 seats" - clean and translatable
            val capacityText = itemView.context.getString(
                R.string.room_capacity_format,
                room.roomName,
                room.roomCapacity
            )
            tvRoomName.text = capacityText

            // Long click to edit room
            itemView.setOnLongClickListener {
                val dialog = EditRoomDialogFragment.newInstance(
                    room.roomId,
                    room.roomName,
                    room.roomCapacity
                )
                dialog.setOnRoomUpdatedListener {
                    onRoomUpdated()
                }
                dialog.show(activity.supportFragmentManager, "EditRoomDialog")
                true
            }
        }
    }
}