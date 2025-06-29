package com.example.soulvent.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase
import com.example.soulvent.model.Comment
import com.example.soulvent.model.Post
import com.example.soulvent.model.Reaction
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class PostRepository {
    private val db = FirebaseFirestore.getInstance()
    private val database = Firebase.database.reference
    private val postsCollection = db.collection("vents")
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"

    suspend fun addPost(content: String, mood: String) {
        val postId = postsCollection.document().id
        val post = Post(
            id = postId,
            userId = userId,
            content = content,
            commentCount = 0,
            likeCount = 0,
            mood = mood
        )
        postsCollection.document(post.id).set(post).await()
        database.child("posts").child(postId).setValue(post)
    }

    fun getPosts(): Flow<List<Post>> = callbackFlow {
        val subscription = postsCollection
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val posts = snapshot.documents.mapNotNull { document ->
                        document.toObject(Post::class.java)?.copy(id = document.id)
                    }
                    trySend(posts).isSuccess
                }
            }

        awaitClose { subscription.remove() }
    }


    suspend fun addComment(postId: String, content: String) {
        val comment = Comment(
            id = postsCollection.document(postId).collection("comments").document().id,
            postId = postId,
            userId = userId,
            content = content
        )

        val postRef = postsCollection.document(postId)

        db.runTransaction { transaction ->
            val postSnapshot = transaction.get(postRef)
            if (postSnapshot.exists()) {
                transaction.set(postRef.collection("comments").document(comment.id), comment)
                transaction.update(postRef, "commentCount", FieldValue.increment(1))
            } else {
                throw Exception("Post with ID $postId not found.")
            }
        }.await()
    }


    fun getCommentsForPost(postId: String): Flow<List<Comment>> = callbackFlow {
        val subscription = postsCollection.document(postId)
            .collection("comments")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val comments = snapshot.documents.mapNotNull { document ->
                        document.toObject(Comment::class.java)?.copy(id = document.id)
                    }
                    trySend(comments).isSuccess
                }
            }

        awaitClose { subscription.remove() }
    }


    suspend fun toggleLike(postId: String) {
        val reactionRef = postsCollection.document(postId)
            .collection("reactions")
            .document(userId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(reactionRef)
            if (snapshot.exists()) {
                transaction.delete(reactionRef)
                transaction.update(postsCollection.document(postId), "likeCount", FieldValue.increment(-1))
            } else {
                val reaction = Reaction(
                    id = userId,
                    postId = postId,
                    userId = userId,
                    type = "like"
                )
                transaction.set(reactionRef, reaction)
                transaction.update(postsCollection.document(postId), "likeCount", FieldValue.increment(1))
            }
            null
        }.await()
    }

    suspend fun hasUserLikedPost(postId: String): Boolean {
        if (userId == "unknown" || userId == "anonymous") return false

        return postsCollection.document(postId)
            .collection("reactions")
            .document(userId)
            .get()
            .await()
            .exists()
    }

    // --- New Functions for User Blocking ---

    fun getCurrentUserId(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }

    fun getBlockedUsersFlow(userId: String): Flow<List<String>> = callbackFlow {
        val userDocRef = db.collection("users").document(userId)

        val subscription = userDocRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val blockedUsers = if (snapshot != null && snapshot.exists()) {
                snapshot.get("blockedUsers") as? List<String> ?: emptyList()
            } else {
                emptyList()
            }
            trySend(blockedUsers).isSuccess
        }
        awaitClose { subscription.remove() }
    }

    suspend fun blockUser(userIdToBlock: String) {
        val currentUserId = getCurrentUserId() ?: return
        val userDocRef = db.collection("users").document(currentUserId)
        userDocRef.set(
            mapOf("blockedUsers" to FieldValue.arrayUnion(userIdToBlock)),
            SetOptions.merge()
        ).await()
    }
}