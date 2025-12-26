package com.example.yourway.fetchpost
data class Comment(
    var commentText: String = "",
    var timestamp: Long = System.currentTimeMillis(),
    var username: String? = null,
    var likes: Int = 0,
    var commentId: String = ""
)
