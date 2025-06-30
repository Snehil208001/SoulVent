package com.example.soulvent.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp

data class Post(
    val id: String = "",
    val userId: String = "",
    val content: String = "",
    @ServerTimestamp
    val timestamp: Timestamp? = null,
    val commentCount: Int = 0,
    @get:PropertyName("reactions")
    val reactions: Map<String, Int> = emptyMap(),
    val mood: String = "",
    val tags: List<String> = emptyList(),
    val imageUrl: String? = null, // Add this line
    val lastEdited: Timestamp? = null,
    val reportCount: Int = 0
)