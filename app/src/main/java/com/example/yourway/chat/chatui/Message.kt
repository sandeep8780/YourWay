package com.example.yourway.chat.chatui

data class Message(
    val id: String = "",
    val senderId: String = "",
    val displayName: String = "",
    val content: String = "",
    val timestamp: Long = 0L,
    val type: String = "text" // Could be "text" or "media"
)

