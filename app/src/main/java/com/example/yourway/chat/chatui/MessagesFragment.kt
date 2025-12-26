package com.example.yourway.chat.chatui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yourway.R
import com.example.yourway.chat.SharedPrefsHelper
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MessagesFragment : Fragment() {

    private lateinit var messagesAdapter: MessagesAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var chatId: String
    private val messagesList = mutableListOf<Message>()
    private val firestore = FirebaseFirestore.getInstance()
    private var lastVisible: DocumentSnapshot? = null
    private val pageSize = 100

    companion object {
        private const val ARG_CHAT_ID = "chatId"

        fun newInstance(chatId: String): MessagesFragment {
            val fragment = MessagesFragment()
            val args = Bundle()
            args.putString(ARG_CHAT_ID, chatId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_messages, container, false)
        chatId = arguments?.getString(ARG_CHAT_ID) ?: throw IllegalArgumentException("Chat ID not provided")

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.rv_chatui_messages)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Set up adapter
        messagesAdapter =
            SharedPrefsHelper.getUsername(requireContext())
                ?.let { MessagesAdapter(messagesList, it) }!!
        recyclerView.adapter = messagesAdapter

        Toast.makeText(requireContext(), "Loading messages...", Toast.LENGTH_SHORT).show()

//        loadMessages()

        // Listen for real-time updates
        listenForMessages()

        return view
    }

    private fun loadMessages() {
        messagesList.clear()

        firestore.collection("chats").document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(pageSize.toLong())
            .get()
            .addOnSuccessListener { documents ->
                if (documents != null && !documents.isEmpty) {
                    lastVisible = documents.documents[documents.size() - 1]
                    val newMessages = documents.toObjects(Message::class.java)
                    messagesList.addAll(newMessages) // Add new messages
                    messagesAdapter.notifyDataSetChanged()
                    Log.d("MessagesFragment", "Messages loaded: ${newMessages.size}")
                    Toast.makeText(requireContext(), "Messages loaded", Toast.LENGTH_SHORT).show()
                } else {
                    Log.d("MessagesFragment", "No messages found")
                    Toast.makeText(requireContext(), "No messages found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("MessagesFragment", "Error loading messages: ", e)
                Toast.makeText(requireContext(), "Error loading messages", Toast.LENGTH_SHORT).show()
            }
    }

    private fun listenForMessages() {
        firestore.collection("chats").document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .limitToLast(pageSize.toLong())
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("MessagesFragment", "Listen failed.", e)
                    Toast.makeText(requireContext(), "Listen failed", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshots != null && !snapshots.isEmpty) {
                    val newMessages = snapshots.toObjects(Message::class.java)
                    messagesList.clear()
                    messagesList.addAll(newMessages)
                    messagesAdapter.notifyDataSetChanged()
                    recyclerView.scrollToPosition(messagesList.size - 1)
                    Log.d("MessagesFragment", "Real-time messages updated")
                } else {
                    Log.d("MessagesFragment", "No new messages found")
                }
            }
    }

    // Method to send a message
    fun sendMessage(messageText: String) {
        val username = SharedPrefsHelper.getUsername(requireContext())
        val displayName = SharedPrefsHelper.getDisplayName(requireContext())

        val message = Message(
            id = firestore.collection("chats").document(chatId).collection("messages").document().id,
            senderId = username ?: "Unknown",
            displayName = displayName ?: "Unknown",
            content = messageText,
            timestamp = System.currentTimeMillis()
        )

        firestore.collection("chats").document(chatId)
            .collection("messages")
            .add(message)
            .addOnSuccessListener {
                Log.d("MessagesFragment", "Message sent successfully")


                // Update the last message and last message time in chat document
                val updates = hashMapOf<String, Any>(
                    "lastMessage" to messageText,
                    "lastMessageTime" to System.currentTimeMillis()
                )

                firestore.collection("chats").document(chatId)
                    .update(updates)
                    .addOnSuccessListener {
                        Log.d("MessagesFragment", "Chat updated with last message and time")
                    }
                    .addOnFailureListener { e ->
                        Log.e("MessagesFragment", "Error updating chat: ", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("MessagesFragment", "Error sending message: ", e)
                Toast.makeText(requireContext(), "Error sending message", Toast.LENGTH_SHORT).show()
            }
    }

    // Pagination: Call this method to load older messages
    fun loadMoreMessages() {
        lastVisible?.let {
            firestore.collection("chats").document(chatId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .startAfter(it)
                .limit(pageSize.toLong())
                .get()
                .addOnSuccessListener { documents ->
                    if (documents != null && !documents.isEmpty) {
                        lastVisible = documents.documents[documents.size() - 1]
                        val newMessages = documents.toObjects(Message::class.java)
                        messagesList.addAll(0, newMessages)
                        messagesAdapter.notifyDataSetChanged()
                        Log.d("MessagesFragment", "Loaded more messages: ${newMessages.size}")
                        Toast.makeText(requireContext(), "Loaded more messages", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.d("MessagesFragment", "No more messages to load")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("MessagesFragment", "Error loading more messages: ", e)
                    Toast.makeText(requireContext(), "Error loading more messages", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
