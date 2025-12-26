package com.example.yourway.forum
data class ForumThread(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val createdBy: String = "", // User ID of the person who created the thread
    val comments: List<Comment> = listOf() // List of comments associated with the thread
)

