package com.example.schedulingSystem.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.schedulingSystem.R

class TimeTableAdapter(
    private val timeRanges: List<String>, // e.g. "7:00 AM – 8:30 AM", "8:30 AM – 10:00 AM"
    private val scheduleMap: MutableMap<String, MutableList<com.example.schedulingSystem.fragments.TimeTableScheduleItem>> // day_timeRange → list of classes
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
        val timeRange = timeRanges[position]
        holder.tvTime.text = timeRange

        val days = arrayOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        for (i in days.indices) {
            val key = "${days[i]}_${timeRange}"
            val classes = scheduleMap[key] ?: emptyList()
            
            // Format: Subject\nSection\nTeacher
            holder.cells[i].text = if (classes.isEmpty()) "" else classes.joinToString("\n") { 
                "${it.subject}\n${it.section}\n${it.teacher}"
            }
            holder.cells[i].setBackgroundColor(
                if (classes.isNotEmpty()) 0xFFF0F4FF.toInt() else 0xFFFFFFFF.toInt()
            )
        }
    }

    override fun getItemCount() = timeRanges.size
}