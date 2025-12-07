package com.example.schedulingSystem.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.schedulingSystem.R
import com.example.schedulingSystem.ScheduleItem


//val ScheduleItem.sectionDisplay: String
//    get() = "${sectionName ?: "Unknown"} (Year $sectionYear)"
//
//val ScheduleItem.subjectDisplay: String
//    get() = subjectName?.uppercase() ?: "No Subject Name"


class TimeTableAdapter(
    private val timeSlots: List<String>, // e.g. "7:00 AM", "7:30 AM"
    private val scheduleMap: Map<String, List<ScheduleItem>> // day_time → list of classes
) : RecyclerView.Adapter<TimeTableAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTime = view.findViewById<TextView>(R.id.tvTime)
        val cells = arrayOf(
            view.findViewById<TextView>(R.id.tvMon),
            view.findViewById<TextView>(R.id.tvTue),
            view.findViewById<TextView>(R.id.tvWed),
            view.findViewById<TextView>(R.id.tvThu),
            view.findViewById<TextView>(R.id.tvFri),
            view.findViewById<TextView>(R.id.tvSat),
            view.findViewById<TextView>(R.id.tvSun)
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_time_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val time = timeSlots[position]
        holder.tvTime.text = time

        val days = arrayOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        for (i in days.indices) {
            val key = "${days[i]}_$time"
            val classes = scheduleMap[key] ?: emptyList()
            holder.cells[i].text = if (classes.isEmpty()) "Free" else classes.joinToString("\n") { "${it.subjectName}\n${it.sectionName}" }
            holder.cells[i].setBackgroundColor(
                if (classes.isNotEmpty()) 0xFFE3F2FD.toInt() else 0xFFFFFFFF.toInt()
            )

        }
    }

    override fun getItemCount() = timeSlots.size
}