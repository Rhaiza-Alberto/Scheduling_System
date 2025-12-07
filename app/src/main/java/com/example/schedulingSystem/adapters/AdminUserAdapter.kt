package com.example.schedulingSystem.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.schedulingSystem.R
import com.example.schedulingSystem.models.UserItem
import java.util.Locale

class AdminUserAdapter : ListAdapter<UserItem, AdminUserAdapter.UserViewHolder>(UserDiffCallback()) {

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
        private val tvStatus = itemView.findViewById<TextView>(R.id.tvUserStatus)

        fun bind(user: UserItem) {
            tvName.text = user.fullName
            tvEmail.text = user.email
            tvType.text = user.accountType.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            }
            tvStatus.text = user.status
            tvStatus.setTextColor(
                if (user.status == "Approved")
                    itemView.context.getColor(R.color.primary_dark_green)
                else
                    itemView.context.getColor(R.color.orange)
            )
        }
    }

    class UserDiffCallback : DiffUtil.ItemCallback<UserItem>() {
        override fun areItemsTheSame(oldItem: UserItem, newItem: UserItem): Boolean =
            oldItem.userId == newItem.userId

        override fun areContentsTheSame(oldItem: UserItem, newItem: UserItem): Boolean =
            oldItem == newItem
    }
}