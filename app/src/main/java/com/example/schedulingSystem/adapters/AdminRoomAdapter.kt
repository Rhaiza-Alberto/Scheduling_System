package com.example.schedulingSystem.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
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

class AdminUserAdapter(
    private val activity: FragmentActivity,
    private val onUserUpdated: () -> Unit
) : ListAdapter<User, AdminUserAdapter.UserViewHolder>(UserDiffCallback()) {

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

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvUserName)
        private val tvEmail: TextView = itemView.findViewById(R.id.tvUserEmail)
        private val tvType: TextView = itemView.findViewById(R.id.tvUserType)
        private val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit1)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete1)

        fun bind(user: User) {
            tvName.text = user.fullName
            tvEmail.text = user.email

            // Capitalize role
            tvType.text = user.accountType.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase() else it.toString()
            }

            val isAdmin = user.accountType.equals("admin", ignoreCase = true)
            tvType.setTextColor(
                itemView.context.getColor(if (isAdmin) R.color.orange else R.color.primary_dark_green)
            )

            // EDIT BUTTON (you can implement later)
            btnEdit.setOnClickListener {
                Toast.makeText(itemView.context, "Edit user - Coming soon", Toast.LENGTH_SHORT).show()
            }

            // DELETE BUTTON → Soft Delete
            btnDelete.setOnClickListener {
                androidx.appcompat.app.AlertDialog.Builder(itemView.context)
                    .setTitle("Remove User")
                    .setMessage("Remove \"${user.fullName}\" from the system?\n\nThis is reversible.")
                    .setPositiveButton("Remove") { _, _ ->
                        softDeleteUser(user.personId)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }

        private fun softDeleteUser(personId: Int) {
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
                            onUserUpdated() // Refresh the list
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            itemView.context,
                            "Delete failed: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
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