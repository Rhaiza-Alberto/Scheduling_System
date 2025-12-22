package com.example.schedulingSystem.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.schedulingSystem.R
import com.example.schedulingSystem.models.TimetableItem

interface OnScheduleClickListener {
    fun onScheduleClick(scheduleId: Int, dayName: String, timeSlot: String, status: String)
    fun onEmptySlotClick(dayName: String, timeSlot: String)
}

class TimetableGridAdapter(
    private val clickListener: OnScheduleClickListener? = null
) : ListAdapter<TimetableItem, RecyclerView.ViewHolder>(TimetableDiffCallback()) {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_TIME = 1
        private const val TYPE_EMPTY = 2
        private const val TYPE_BLOCK = 3
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is TimetableItem.Header -> TYPE_HEADER
            is TimetableItem.TimeLabel -> TYPE_TIME
            is TimetableItem.Empty -> TYPE_EMPTY
            is TimetableItem.ClassBlock -> TYPE_BLOCK
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_HEADER -> HeaderViewHolder(inflater.inflate(R.layout.item_timetable_header, parent, false))
            TYPE_TIME -> TimeViewHolder(inflater.inflate(R.layout.item_timetable_time, parent, false))
            TYPE_EMPTY -> EmptyViewHolder(inflater.inflate(R.layout.item_timetable_empty, parent, false))
            TYPE_BLOCK -> BlockViewHolder(inflater.inflate(R.layout.item_timetable_block, parent, false))
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is HeaderViewHolder -> holder.bind(item as TimetableItem.Header)
            is TimeViewHolder -> holder.bind(item as TimetableItem.TimeLabel)
            is BlockViewHolder -> holder.bind(item as TimetableItem.ClassBlock, clickListener)
            is EmptyViewHolder -> {
                // Calculate day and time for empty slots
                val numColumns = if (itemCount > 50) 8 else 2 // Rough estimate for week/day view
                val row = (position - numColumns) / numColumns
                val col = position % numColumns
                
                if (row >= 0 && col > 0) { // Valid grid position
                    val dayName = if (numColumns == 8) {
                        listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")[col - 1]
                    } else {
                        // Day view - use current day (this would need to be passed in)
                        ""
                    }
                    val timeSlot = listOf("7:00 AM", "7:30 AM", "8:00 AM", "8:30 AM", "9:00 AM", "9:30 AM",
                        "10:00 AM", "10:30 AM", "11:00 AM", "11:30 AM", "12:00 PM", "12:30 PM",
                        "1:00 PM", "1:30 PM", "2:00 PM", "2:30 PM", "3:00 PM", "3:30 PM",
                        "4:00 PM", "4:30 PM", "5:00 PM", "5:30 PM", "6:00 PM", "6:30 PM", "7:00 PM").getOrNull(row) ?: ""
                    
                    holder.bind(dayName, timeSlot, clickListener)
                }
            }
        }
    }

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvHeader: TextView = view.findViewById(R.id.tvHeader)
        fun bind(item: TimetableItem.Header) {
            tvHeader.text = item.text
        }
    }

    class TimeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvTimeLabel: TextView = view.findViewById(R.id.tvTimeLabel)
        fun bind(item: TimetableItem.TimeLabel) {
            tvTimeLabel.text = item.time
        }
    }

    class EmptyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        init {
            // Set light green background for empty slots
            view.setBackgroundResource(R.drawable.bg_empty_slot)
        }
        
        fun bind(dayName: String, timeSlot: String, clickListener: OnScheduleClickListener?) {
            itemView.setOnClickListener {
                clickListener?.onEmptySlotClick(dayName, timeSlot)
            }
        }
    }

    class BlockViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvSubject: TextView = view.findViewById(R.id.tvSubject)
        private val tvSection: TextView = view.findViewById(R.id.tvSection)
        private val tvTeacher: TextView = view.findViewById(R.id.tvTeacher)
        private val container = view.findViewById<View>(R.id.classBlockContainer)

        fun bind(item: TimetableItem.ClassBlock, clickListener: OnScheduleClickListener?) {
            tvSubject.text = item.subject
            tvSection.text = item.section
            tvTeacher.text = item.teacher
            
            // Set background color based on status
            val context = container.context
            when (item.status.lowercase()) {
                "pending" -> container.setBackgroundResource(R.drawable.bg_class_block_pending)
                "occupied" -> container.setBackgroundResource(R.drawable.bg_class_block_occupied)
                else -> container.setBackgroundResource(R.drawable.bg_class_block_pending)
            }
            
            // Handle rowSpan height (each slot is approx 60dp + margins)
            // We'll approximate 60dp per span + some compensation for margins if needed
            // The layouts have 1dp margin.
            val density = itemView.context.resources.displayMetrics.density
            val heightDp = 60 * item.rowSpan
            val params = itemView.layoutParams
            params.height = (heightDp * density).toInt()
            itemView.layoutParams = params
            
            // Set click listener on the container instead of the entire item view
            container.setOnClickListener {
                clickListener?.onScheduleClick(item.scheduleId, item.dayName, item.timeSlot, item.status)
            }
        }
    }

    class TimetableDiffCallback : DiffUtil.ItemCallback<TimetableItem>() {
        override fun areItemsTheSame(oldItem: TimetableItem, newItem: TimetableItem): Boolean {
            return oldItem == newItem
        }
        override fun areContentsTheSame(oldItem: TimetableItem, newItem: TimetableItem): Boolean {
            return oldItem == newItem
        }
    }
}