package com.example.yourway.forum

data class Forum(
    var id: String = "", // Unique ID for the forum (discussion thread)
    val title: String = "", // Title of the forum thread
    val description: String = "", // Description or main content of the forum thread
    val createdBy: String = "", // User ID of the person who created the thread
    val createdAt: Long = System.currentTimeMillis(), // Timestamp for creation time
    val upvotes: Int = 0, // Number of upvotes
    val downvotes: Int = 0, // Number of downvotes
    val views: Int = 0, // Number of times the forum is viewed
    val commentCount: Int = 0,
    val comments: List<String> = listOf(), // List of comment IDs (references to comments)
)
