package com.example.yourway.chat.chatui

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.yourway.R
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.Locale

class MessagesAdapter(
    private var messages: List<Message>,
    private val currentUserId: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_USER_MESSAGE = 1
        private const val VIEW_TYPE_OTHER_MESSAGE = 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_USER_MESSAGE) {
            val view = inflater.inflate(R.layout.item_chatui_user_message, parent, false)
            UserMessageViewHolder(view)
        } else {
            val view = inflater.inflate(R.layout.item_chatui_other_message, parent, false)
            OtherMessageViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        Log.d("MessagesAdapter", "Total items in adapter: ${messages.size}")

        when (holder) {
            is UserMessageViewHolder -> holder.bind(message)
            is OtherMessageViewHolder -> holder.bind(message)
        }

        Log.d("MessagesAdapter", "Binding message at position: $position, content: ${message.content}")
        Toast.makeText(holder.itemView.context, "Binding message: ${message.content}", Toast.LENGTH_SHORT).show()
    }

    override fun getItemCount(): Int = messages.size

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].senderId == currentUserId) {
            VIEW_TYPE_USER_MESSAGE
        } else {
            VIEW_TYPE_OTHER_MESSAGE
        }
    }

    // Method to update the messages list
    fun updateMessages(newMessages: List<Message>) {
        messages = newMessages
        notifyDataSetChanged() // Consider notifying more selectively for efficiency
    }

    class UserMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.tv_chatui_user_messageText)
        private val timestamp: TextView = itemView.findViewById(R.id.tv_chatui_user_timestamp)

        fun bind(message: Message) {
            messageText.text = message.content
            timestamp.text = formatTimestamp(message.timestamp)
        }

        private fun formatTimestamp(timestamp: Long): String {
            val date = Date(timestamp)
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            return format.format(date)
        }
    }

    class OtherMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val displayName: TextView = itemView.findViewById(R.id.tv_chatui_other_displayName)
        private val messageText: TextView = itemView.findViewById(R.id.tv_chatui_other_messageText)
        private val timestamp: TextView = itemView.findViewById(R.id.tv_chatui_other_timestamp)

        fun bind(message: Message) {
            displayName.text = message.displayName
            messageText.text = message.content
            timestamp.text = formatTimestamp(message.timestamp)
        }

        private fun formatTimestamp(timestamp: Long): String {
            val date = Date(timestamp)
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            return format.format(date)
        }
    }
}
