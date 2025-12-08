package com.example.schedulingSystem.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.schedulingSystem.R
import com.example.schedulingSystem.models.User
import java.util.Locale

class AdminUserAdapter : ListAdapter<User, AdminUserAdapter.UserViewHolder>(UserDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName = itemView.findViewById<TextView>(R.id.tvUserName)
        private val tvEmail = itemView.findViewById<TextView>(R.id.tvUserEmail)
        private val tvType = itemView.findViewById<TextView>(R.id.tvUserType)
        //private val tvStatus = itemView.findViewById<TextView>(R.id.tvUserStatus) // Optional

        fun bind(user: User) {
            tvName.text = user.fullName
            tvEmail.text = user.email

            // Capitalize account type: "teacher" → "Teacher"
            tvType.text = user.accountType.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            }

            // Optional: Show status (you can remove if not needed)
            // Since your API doesn't have approval status, we'll show account type color
            val context = itemView.context
            val isAdmin = user.accountType.equals("admin", ignoreCase = true)
            tvType.setTextColor(
                context.getColor(if (isAdmin) R.color.orange else R.color.primary_dark_green)
            )

            // Optional status (you can hide this view if not used)
//            tvStatus?.let {
//                it.text = if (isAdmin) "Administrator" else "Active"
//                it.setTextColor(context.getColor(if (isAdmin) R.color.orange else R.color.primary_dark_green))
//            }
        }
    }

    class UserDiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean =
            oldItem.personId == newItem.personId

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean =
            oldItem == newItem
    }
}