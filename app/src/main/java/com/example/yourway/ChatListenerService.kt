package com.example.yourway

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.yourway.chat.chatui.ChatInterfaceActivity
import com.example.yourway.userprofile.SharedPreferencesHelper
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class ChatListenerService : Service() {

    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var currentUserName: String
    private var listenerRegistration: ListenerRegistration? = null

    override fun onCreate() {
        super.onCreate()
        Log.d("ChatListenerService", "Service Created")

        // Retrieve the current username from SharedPreferences
        val sharedPreferencesHelper = SharedPreferencesHelper(applicationContext)
        val userProfile = sharedPreferencesHelper.getUserProfile()
        currentUserName = userProfile?.username.toString()

        // Start listening for chat changes
        Log.d("ChatListenerService", "Starting to listen for chat changes")
        startListeningForChatChanges()
    }

    private fun startListeningForChatChanges() {
        // Listen for changes in the chats collection where the current user is a participant
        firestore.collection("chats")
            .whereArrayContains("participants", currentUserName)
            .get()
            .addOnSuccessListener{ snapshots ->


                // For each chat document, listen to its messages subcollection
                for (docChange in snapshots!!.documentChanges) {
                    val chatDocId = docChange.document.id

                    if (docChange.type == DocumentChange.Type.MODIFIED) {
                        // Listen for changes in the messages subcollection of this chat document
                        listenerRegistration = firestore.collection("chats")
                            .document(chatDocId)
                            .collection("messages")
                            .addSnapshotListener { messageSnapshots, messageError ->

                                if (messageError != null) {
                                    Log.w("Firestore", "Message Listen failed.", messageError)
                                    return@addSnapshotListener
                                }

                                for (messageDocChange in messageSnapshots!!.documentChanges) {
                                    val messageData = messageDocChange.document.data

                                    // Check if the message is not sent by the current user
                                    val senderId = messageData["senderId"] as String
                                    if (senderId != currentUserName) {
                                        Log.d(
                                            "ChatListenerService",
                                            "New message from another user"
                                        )
                                        sendNotification(messageData, chatDocId)
                                    } else {
                                        Log.d(
                                            "ChatListenerService",
                                            "Message from current user, no notification"
                                        )
                                    }
                                }
                            }
                    }
                }
            }
    }

    private fun sendNotification(messageData: Map<String, Any>, chatId: String) {
        // Log the data to inspect its structure
        Log.d("ChatListenerService", "Message data: $messageData")

        // Extract necessary details for the notification
        val messageText = messageData["content"] as? String
            ?: "New message"  // Make sure "text" is the correct key
        val senderId = messageData["senderId"] as? String ?: "Unknown Sender"
        val timestamp = messageData["timestamp"] as? Long ?: System.currentTimeMillis()

        // Create the notification title
        val title = "New message from: $senderId"

        // Create and display the notification
        createNotification(title, messageText, timestamp, chatId)
    }

    private fun createNotification(title: String, message: String, time: Long, chatId: String) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "chat_notifications",
                "Chat Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Create an intent to open ChatInterfaceActivity with chatId passed as an extra
        val intent = Intent(this, ChatInterfaceActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("chatId", chatId)  // Pass chatId to ChatInterfaceActivity
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Create the notification
        val notification = NotificationCompat.Builder(this, "chat_notifications")
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.logo)
            .setWhen(time)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)  // Set the pending intent for notification tap
            .build()

        // Generate a notification ID based on the chatId
        val notificationId = chatId.hashCode()

        // Display the notification
        notificationManager.notify(notificationId, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("ChatListenerService", "onStartCommand called")

        // Create a notification and start the service in the foreground
        val notification = NotificationCompat.Builder(this, "chat_notifications")
//            .setContentTitle("Chat Listener Running")
//            .setContentText("Listening for new messages...")
//            .setSmallIcon(R.drawable.logo)
            .build()

        // Start the service as a foreground service with the notification
        startForeground(1, notification)

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("ChatListenerService", "Service Destroyed")

        // Clean up Firestore listener
        listenerRegistration?.remove()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}


