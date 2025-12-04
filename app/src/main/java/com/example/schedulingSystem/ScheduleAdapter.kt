package com.example.schedulingSystem


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.schedulingSystem.ScheduleItem

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
            .inflate(R.layout.item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        holder.txtDay.text = item.dayName
        holder.txtTime.text = "${item.timeStart} - ${item.timeEnd}"
        holder.txtSubject.text = "${item.subjectCode} - ${item.subjectName}"
        holder.txtSection.text = item.sectionName
        holder.txtRoom.text = item.roomName
        holder.txtTeacher.text = item.teacherFullName
    }

    override fun getItemCount() = list.size
}
