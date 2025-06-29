package com.example.soulvent.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class Post(
    val id: String = "",
    val userId: String = "",
    val content: String = "",
    @ServerTimestamp
    val timestamp: Timestamp? = null,
    val commentCount: Int = 0,
    val likeCount: Int = 0,
    val mood: String = ""
)