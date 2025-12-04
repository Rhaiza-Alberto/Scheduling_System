package com.example.schedulingSystem.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.schedulingSystem.R
import com.example.schedulingSystem.models.PersonEntity

class PersonAdapter(
    private val onItemClick: ((PersonEntity) -> Unit)? = null) : ListAdapter<PersonEntity, PersonAdapter.PersonViewHolder>(PersonDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_person, parent, false)
        return PersonViewHolder(view)
    }

    override fun onBindViewHolder(holder: PersonViewHolder, position: Int) {
        val person = getItem(position)
        holder.bind(person)
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(person)
        }
    }

    class PersonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvFullName: TextView = itemView.findViewById(R.id.tvFullName)
        private val tvEmail: TextView = itemView.findViewById(R.id.tvEmail)
        private val tvPersonId: TextView = itemView.findViewById(R.id.tvPersonId)

        fun bind(person: PersonEntity) {
            // Build full name with proper formatting
            val fullName = buildString {
                append(person.nameFirst)
                if (person.nameMiddle.isNotBlank()) append(" ${person.nameMiddle}")
                if (person.nameSecond.isNotBlank()) append(" ${person.nameSecond}")
                append(" ${person.nameLast}")
                if (person.nameSuffix.isNotBlank()) append(" ${person.nameSuffix}")
            }.trim()

            tvFullName.text = fullName
            tvEmail.text = person.personUsername
            tvPersonId.text = itemView.context.getString(R.string.person_id_format, person.personID)
        }
    }
}

// Efficient updates using DiffUtil
class PersonDiffCallback : DiffUtil.ItemCallback<PersonEntity>() {
    override fun areItemsTheSame(oldItem: PersonEntity, newItem: PersonEntity): Boolean {
        return oldItem.personID == newItem.personID
    }

    override fun areContentsTheSame(oldItem: PersonEntity, newItem: PersonEntity): Boolean {
        return oldItem == newItem
    }
}