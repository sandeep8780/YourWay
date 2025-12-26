package com.example.yourway.fetchpost

import com.google.firebase.Timestamp

data class Post(
    var postId: String = "",
    val imageUrls: List<String> = emptyList(),  // Default value as an empty list
    val timestamp: Timestamp = Timestamp.now(),
    val likes: Int = 0,
    val title: String = "",
    val description: String = "",
    val username: String = "",
    val commentCount: Int = 0
)

