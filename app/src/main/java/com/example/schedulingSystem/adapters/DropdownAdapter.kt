package com.example.schedulingSystem.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.TextView
import com.example.schedulingSystem.R

class DropdownAdapter(
    context: Context,
    private val items: List<String>
) : ArrayAdapter<String>(context, 0, items) {

    private var filteredItems: List<String> = items
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    private class ViewHolder(view: View) {
        val textView: TextView = view.findViewById(R.id.dropdownItemText)
    }

    override fun getCount(): Int = filteredItems.size

    override fun getItem(position: Int): String? = filteredItems[position]

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        val viewHolder: ViewHolder

        if (convertView == null) {
            view = inflater.inflate(R.layout.dropdown_item, parent, false)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        viewHolder.textView.text = filteredItems[position]

        return view
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filterResults = FilterResults()

                if (constraint.isNullOrEmpty()) {
                    filteredItems = items
                } else {
                    val query = constraint.toString().lowercase()
                    filteredItems = items.filter {
                        it.lowercase().contains(query)
                    }
                }

                filterResults.values = filteredItems
                filterResults.count = filteredItems.size
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredItems = results?.values as? List<String> ?: items
                notifyDataSetChanged()
            }
        }
    }

    // Optional: Add method to update items dynamically
    fun updateItems(newItems: List<String>) {
        clear()
        addAll(newItems)
        notifyDataSetChanged()
    }
}