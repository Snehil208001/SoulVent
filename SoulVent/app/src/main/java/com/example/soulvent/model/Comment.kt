package com.example.soulvent.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class Comment(
    val id: String = "",
    val postId: String = "",
    val userId: String = "",
    val content: String = "",
    @ServerTimestamp val timestamp: Timestamp? = null
)