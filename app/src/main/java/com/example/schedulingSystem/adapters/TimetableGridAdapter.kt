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

class TimetableGridAdapter : ListAdapter<TimetableItem, RecyclerView.ViewHolder>(TimetableDiffCallback()) {

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
            is BlockViewHolder -> holder.bind(item as TimetableItem.ClassBlock)
            is EmptyViewHolder -> { }
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

    class EmptyViewHolder(view: View) : RecyclerView.ViewHolder(view)

    class BlockViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvSubject: TextView = view.findViewById(R.id.tvSubject)
        private val tvSection: TextView = view.findViewById(R.id.tvSection)
        private val tvTeacher: TextView = view.findViewById(R.id.tvTeacher)

        fun bind(item: TimetableItem.ClassBlock) {
            tvSubject.text = item.subject
            tvSection.text = item.section
            tvTeacher.text = item.teacher
            
            // Handle rowSpan height (each slot is approx 60dp + margins)
            // We'll approximate 60dp per span + some compensation for margins if needed
            // The layouts have 1dp margin.
            val density = itemView.context.resources.displayMetrics.density
            val heightDp = 60 * item.rowSpan
            val params = itemView.layoutParams
            params.height = (heightDp * density).toInt()
            itemView.layoutParams = params
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