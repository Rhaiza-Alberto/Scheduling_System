package com.example.schedulingSystem.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.schedulingSystem.R
import com.example.schedulingSystem.models.RoomSchedule

class RoomAdapter(
    private val rooms: List<RoomSchedule>
) : RecyclerView.Adapter<RoomAdapter.RoomViewHolder>() {

    class RoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtDay: TextView = itemView.findViewById(R.id.txtDay)
        val txtTime: TextView = itemView.findViewById(R.id.txtTime)
        val txtSubject: TextView = itemView.findViewById(R.id.txtSubject)
        val txtSection: TextView = itemView.findViewById(R.id.txtSection)
        val txtTeacher: TextView = itemView.findViewById(R.id.txtTeacher)
        val txtRoom: TextView = itemView.findViewById(R.id.txtRoom)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_layout, parent, false)
        return RoomViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        val room = rooms[position]
        holder.txtDay.text = room.day
        holder.txtTime.text = room.time
        holder.txtSubject.text = room.subject
        holder.txtSection.text = room.section
        holder.txtTeacher.text = room.teacher
        holder.txtRoom.text = room.room
    }

    override fun getItemCount(): Int = rooms.size
}
