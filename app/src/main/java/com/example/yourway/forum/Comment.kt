package com.example.yourway.forum
data class Comment(

    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val username: String = "", // User ID of the person who made the comment
    val likes: Int = 0,
    var id: String = "",
    val parentId: String? = null, // ID of the parent comment, if it's a reply
    var replies: MutableList<Comment> = mutableListOf()
)

