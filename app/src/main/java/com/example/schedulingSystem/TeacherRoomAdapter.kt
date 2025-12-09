package com.example.schedulingSystem

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.schedulingSystem.models.TeacherScheduleItem

class TeacherScheduleAdapterV2 : ListAdapter<TeacherScheduleItem, TeacherScheduleAdapterV2.ViewHolder>(ScheduleDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_teacher_schedule, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val statusIndicator: View = itemView.findViewById(R.id.statusIndicator)
        private val tvDay: TextView = itemView.findViewById(R.id.tvDay)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val tvSubject: TextView = itemView.findViewById(R.id.tvSubject)
        private val tvSection: TextView = itemView.findViewById(R.id.tvSection)
        private val tvRoomName: TextView = itemView.findViewById(R.id.tvRoomName)

        fun bind(schedule: TeacherScheduleItem) {
            // Day
            tvDay.text = schedule.dayName

            // Time
            tvTime.text = "${schedule.timeStart} - ${schedule.timeEnd}"

            // Subject (Code + Name)
            tvSubject.text = "${schedule.subjectCode} - ${schedule.subjectName}"

            // Section (Name + Year)
            tvSection.text = "${schedule.sectionName} (${schedule.sectionYear})"

            // Room
            tvRoomName.text = schedule.roomName

            // Status Badge and Indicator Color
            if (schedule.isToday) {
                tvStatus.visibility = View.VISIBLE
                tvStatus.text = "Today"
                statusIndicator.setBackgroundColor(itemView.context.getColor(R.color.primary_green))
            } else {
                tvStatus.visibility = View.GONE
                statusIndicator.setBackgroundColor(itemView.context.getColor(R.color.text_grey_light))
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
}