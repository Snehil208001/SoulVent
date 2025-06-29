package com.example.soulvent.model // Corrected package name
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Reaction(
    val id: String = "",
    val postId: String = "",
    val userId: String = "",
    val type: String = "like",
    @ServerTimestamp
    val timestamp: Date? = null
)