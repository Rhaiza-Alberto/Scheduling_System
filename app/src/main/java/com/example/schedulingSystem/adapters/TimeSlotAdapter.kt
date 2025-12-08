package com.example.schedulingSystem.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.schedulingSystem.R
import com.example.schedulingSystem.models.TimeSlotData

class TimeSlotAdapter(
    private val timeSlotDataList: List<TimeSlotData>
) : RecyclerView.Adapter<TimeSlotAdapter.ViewHolder>() {

    // Track which rows should be hidden (merged into previous row)
    private val hiddenRows = mutableSetOf<Int>()
    // Track row heights for merged cells
    private val rowHeights = mutableMapOf<Int, Int>()

    init {
        calculateMergedRows()
    }

    private fun calculateMergedRows() {
        val days = arrayOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        
        for (dayIndex in days.indices) {
            var i = 0
            while (i < timeSlotDataList.size) {
                val currentContent = timeSlotDataList[i].daySchedules[days[dayIndex]]
                
                // Skip if already marked as hidden or if it's free
                if (hiddenRows.contains(i) || currentContent?.isFree == true) {
                    i++
                    continue
                }
                
                // Count consecutive slots with same subject
                var mergeCount = 1
                var j = i + 1
                while (j < timeSlotDataList.size) {
                    val nextContent = timeSlotDataList[j].daySchedules[days[dayIndex]]
                    
                    if (nextContent != null && 
                        !nextContent.isFree && 
                        nextContent.subject == currentContent?.subject &&
                        nextContent.section == currentContent.section &&
                        nextContent.teacher == currentContent.teacher) {
                        mergeCount++
                        j++
                    } else {
                        break
                    }
                }
                
                // Mark rows to hide and set height for first row
                if (mergeCount > 1) {
                    for (k in 1 until mergeCount) {
                        hiddenRows.add(i + k)
                    }
                    rowHeights[i] = mergeCount
                }
                
                i += mergeCount
            }
        }
    }

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
        // Hide merged rows
        if (hiddenRows.contains(position)) {
            holder.itemView.visibility = View.GONE
            holder.itemView.layoutParams.height = 0
            return
        }

        holder.itemView.visibility = View.VISIBLE
        
        val timeSlotData = timeSlotDataList[position]
        holder.tvTime.text = timeSlotData.timeSlot
        
        // Adjust height if this row spans multiple slots
        val spanCount = rowHeights[position] ?: 1
        if (spanCount > 1) {
            val layoutParams = holder.itemView.layoutParams
            layoutParams.height = 60 * spanCount  // 60dp per slot
            holder.itemView.layoutParams = layoutParams
        }

        val days = arrayOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        
        for (i in days.indices) {
            val dayContent = timeSlotData.daySchedules[days[i]]
            
            if (dayContent != null) {
                val displayText = if (dayContent.isFree) {
                    "Free"
                } else {
                    "${dayContent.subject}\n${dayContent.section}" + 
                    (if (dayContent.teacher != null) "\n${dayContent.teacher}" else "")
                }
                
                holder.cells[i].text = displayText
                
                // Color: light blue for scheduled, white for free
                holder.cells[i].setBackgroundColor(
                    if (dayContent.isFree) 0xFFFFFFFF.toInt() else 0xFFF0F4FF.toInt()
                )
                
                // Text color: gray for free, dark for scheduled
                holder.cells[i].setTextColor(
                    if (dayContent.isFree) 0xFFCCCCCC.toInt() else 0xFF333333.toInt()
                )
            } else {
                holder.cells[i].text = "Free"
                holder.cells[i].setBackgroundColor(0xFFFFFFFF.toInt())
                holder.cells[i].setTextColor(0xFFCCCCCC.toInt())
            }
        }
    }

    override fun getItemCount() = timeSlotDataList.size
}
