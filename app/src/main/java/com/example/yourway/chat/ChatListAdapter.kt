package com.example.yourway.chat

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.yourway.R
import com.google.firebase.firestore.FirebaseFirestore

class ChatListAdapter(
    private val chats: MutableList<Chat>,
    private val context: Context,
    private val onChatClick: (Chat) -> Unit
) : RecyclerView.Adapter<ChatListAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvLastMessage: TextView = itemView.findViewById(R.id.tvChatUlLastMessage)
        private val tvDisplayName: TextView = itemView.findViewById(R.id.tvChatUlDisplayName)
        private val ivProfileImage: ImageView = itemView.findViewById(R.id.ivChatUlProfileImage)

        fun bind(chat: Chat) {
            val currentUser = SharedPrefsHelper.getUsername(context)

            if (chat.chatType == "private") {
                val otherUser = chat.participants.firstOrNull { it != currentUser } ?: currentUser
                fetchUser(otherUser) { user ->
                    tvDisplayName.text = user.displayName
                    Glide.with(itemView.context)
                        .load(user.imageSrc)
                        .circleCrop()
                        .placeholder(R.drawable.placeholder_image)
                        .into(ivProfileImage)
                }
            } else if (chat.chatType == "group") {
                tvDisplayName.text = chat.groupName
                ivProfileImage.setImageResource(R.drawable.placeholder_image)
                Glide.with(itemView.context)
                    .load(chat.imageSrc)
                    .circleCrop()
                    .placeholder(R.drawable.placeholder_image)
                    .into(ivProfileImage)
            }

            tvLastMessage.text = chat.lastMessage
            itemView.setOnClickListener {
                onChatClick(chat)
            }
        }

        private fun fetchUser(username: String?, callback: (User) -> Unit) {
            if (username == null) return
            FirebaseFirestore.getInstance().collection("users")
                .document(username)
                .get()
                .addOnSuccessListener { document ->
                    val user = document.toObject(User::class.java)
                    if (user != null) {
                        callback(user)
                    } else {
                        Log.e("ChatListAdapter", "User not found for username: $username")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("ChatListAdapter", "Error fetching user: ", e)
                }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.rv_chat_list_item, parent, false)
        return ChatViewHolder(view)
    }

    override fun getItemCount(): Int = chats.size

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(chats[position])
    }

    fun updateChats(newChats: List<Chat>) {
        chats.clear()
        chats.addAll(newChats)
        notifyDataSetChanged()
    }
}
