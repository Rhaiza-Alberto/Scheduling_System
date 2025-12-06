package com.example.schedulingSystem

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ScheduleAdapter(private val list: List<ScheduleItem>) :
    RecyclerView.Adapter<ScheduleAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtDay: TextView = view.findViewById(R.id.txtDay)
        val txtTime: TextView = view.findViewById(R.id.txtTime)
        val txtSubject: TextView = view.findViewById(R.id.txtSubject)
        val txtSection: TextView = view.findViewById(R.id.txtSection)
        val txtRoom: TextView = view.findViewById(R.id.txtRoom)
        val txtTeacher: TextView = view.findViewById(R.id.txtTeacher)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_schedule, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        holder.txtDay.text = item.dayName
        holder.txtTime.text = "${item.timeStart} - ${item.timeEnd}"

        // Safely handle nullable subject fields
        val subjectText = when {
            item.subjectCode != null && item.subjectName != null ->
                "${item.subjectCode} - ${item.subjectName}"
            item.subjectName != null -> item.subjectName
            item.subjectCode != null -> item.subjectCode
            else -> "No Subject"
        }
        holder.txtSubject.text = subjectText

        // Section
        holder.txtSection.text = item.sectionName ?: "Unknown Section"

        // Room
        holder.txtRoom.text = item.roomName ?: "No Room Assigned"

        // Teacher (uses your computed property — perfect!)
        holder.txtTeacher.text = item.teacherFullName
    }

    override fun getItemCount() = list.size
}