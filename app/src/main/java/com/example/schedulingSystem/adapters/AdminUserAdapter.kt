package com.example.schedulingSystem.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.schedulingSystem.R
import com.example.schedulingSystem.models.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.Locale

class AdminUserAdapter  : ListAdapter<User, AdminUserAdapter.UserViewHolder>(UserDiffCallback()) {

    companion object {
        private const val BACKEND_URL = "http://10.0.2.2/scheduling-api"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_edit_users, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName = itemView.findViewById<TextView>(R.id.tvUserName)
        private val tvEmail = itemView.findViewById<TextView>(R.id.tvUserEmail)
        private val tvType = itemView.findViewById<TextView>(R.id.tvUserType)
        private val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit1)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete1)

        fun bind(user: User) {
            tvName.text = user.fullName
            tvEmail.text = user.email

            // Capitalize account type: "teacher" → "Teacher"
            tvType.text = user.accountType.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            }

            val context = itemView.context
            val isAdmin = user.accountType.equals("admin", ignoreCase = true)
            tvType.setTextColor(
                context.getColor(if (isAdmin) R.color.orange else R.color.primary_dark_green)
            )
            btnDelete.setOnClickListener {
                androidx.appcompat.app.AlertDialog.Builder(itemView.context)
                    .setTitle("Remove Person")
                    .setMessage("Remove \"${user.firstName} ${user.lastName}\" from the list?\n\nYou can restore it later.")
                    .setPositiveButton("Remove") { _, _ ->
                        softDeleteRoom(user.personId)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }

            // Optional status (you can hide this view if not used)
//            tvStatus?.let {
//                it.text = if (isAdmin) "Administrator" else "Active"
//                it.setTextColor(context.getColor(if (isAdmin) R.color.orange else R.color.primary_dark_green))
//            }
        }

        private fun softDeleteRoom(personId: Int) {
            val json = JSONObject().apply { put("person_id", personId) }
            val body = json.toString().toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("$BACKEND_URL/soft_delete_user.php")
                .post(body)
                .build()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = OkHttpClient().newCall(request).execute()
                    val res = response.body?.string() ?: ""

                    withContext(Dispatchers.Main) {
                        val result = JSONObject(res)
                        Toast.makeText(
                            itemView.context,
                            result.getString("message"),
                            Toast.LENGTH_SHORT
                        ).show()

                        if (result.getBoolean("success")) {
                            onRoomUpdated() // Refresh list
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(itemView.context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    class UserDiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean =
            oldItem.personId == newItem.personId

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean =
            oldItem == newItem
    }
}