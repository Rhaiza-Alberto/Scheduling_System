package com.example.schedulingSystem.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.schedulingSystem.R
import com.example.schedulingSystem.models.TeacherScheduleItem

class TeacherScheduleAdapter : ListAdapter<TeacherScheduleItem, TeacherScheduleAdapter.ViewHolder>(ScheduleDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_teacher_schedule, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvSubject: TextView = itemView.findViewById(R.id.tvSubject)
        private val tvSection: TextView = itemView.findViewById(R.id.tvSection)
        private val tvRoomName: TextView = itemView.findViewById(R.id.tvRoomName)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        private val tvDay: TextView = itemView.findViewById(R.id.tvDay)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val statusIndicator: View = itemView.findViewById(R.id.statusIndicator)

        fun bind(schedule: TeacherScheduleItem) {
            // Subject code and name
            tvSubject.text = "${schedule.subjectCode} - ${schedule.subjectName}"

            // Section with year
            tvSection.text = "${schedule.sectionName} (${schedule.sectionYear})"

            // Room
            tvRoomName.text = schedule.roomName

            // Time
            tvTime.text = "${schedule.timeStart} - ${schedule.timeEnd}"

            // Day
            tvDay.text = schedule.dayName

            // Set status and colors
            when {
                schedule.isToday -> {
                    tvStatus.text = "Today"
                    tvStatus.setTextColor(itemView.context.getColor(R.color.primary_green))
                    tvStatus.setBackgroundTintList(itemView.context.getColorStateList(R.color.light_green_bg))
                    statusIndicator.setBackgroundColor(itemView.context.getColor(R.color.primary_green))
                }
                schedule.scheduleStatus.equals("pending", ignoreCase = true) -> {
                    tvStatus.text = "Pending"
                    tvStatus.setTextColor(itemView.context.getColor(R.color.icon_orange))
                    tvStatus.setBackgroundTintList(itemView.context.getColorStateList(R.color.light_orange_bg))
                    statusIndicator.setBackgroundColor(itemView.context.getColor(R.color.icon_orange))
                }
                else -> {
                    tvStatus.text = "Scheduled"
                    tvStatus.setTextColor(itemView.context.getColor(R.color.text_secondary))
                    tvStatus.setBackgroundTintList(itemView.context.getColorStateList(R.color.light_gray_bg))
                    statusIndicator.setBackgroundColor(itemView.context.getColor(R.color.text_secondary))
                }
            }
        }
    }
}

class ScheduleDiffCallback : DiffUtil.ItemCallback<TeacherScheduleItem>() {
    override fun areItemsTheSame(oldItem: TeacherScheduleItem, newItem: TeacherScheduleItem): Boolean {
        return oldItem.scheduleId == newItem.scheduleId
    }

    override fun areContentsTheSame(oldItem: TeacherScheduleItem, newItem: TeacherScheduleItem): Boolean {
        return oldItem == newItem
    }
}