package com.example.yourway.chat

data class Chat(
    var chatId: String = "",
    val displayName: String = "",
    val groupName: String = "",
    val participants: List<String> = listOf(),
    val lastMessage: String = "",
    val lastMessageTime: Long = System.currentTimeMillis(),
    val chatType: String = "private",
    val imageSrc: String = ""
)

