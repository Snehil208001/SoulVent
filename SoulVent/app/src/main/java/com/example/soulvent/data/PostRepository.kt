package com.example.soulvent.data

import android.graphics.Bitmap
import com.example.soulvent.model.Comment
import com.example.soulvent.model.Post
import com.example.soulvent.model.Reaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream

class PostRepository {
    private val db = FirebaseFirestore.getInstance()
    private val database = Firebase.database.reference
    private val postsCollection = db.collection("vents")
    private val storage = Firebase.storage.reference
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"

    suspend fun uploadImage(bitmap: Bitmap): String {
        val imageRef = storage.child("images/${userId}_${System.currentTimeMillis()}.jpg")
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos)
        val data = baos.toByteArray()

        val uploadTask = imageRef.putBytes(data).await()
        return uploadTask.storage.downloadUrl.await().toString()
    }

    suspend fun addPost(content: String, mood: String, tags: List<String>, imageUrl: String?) {
        val postId = postsCollection.document().id
        val post = Post(
            id = postId,
            userId = userId,
            content = content,
            commentCount = 0,
            reactions = emptyMap(),
            mood = mood,
            tags = tags,
            imageUrl = imageUrl
        )
        postsCollection.document(post.id).set(post).await()

        // Realtime DB mirror
        database.child("posts").child(post.id).setValue(post)
    }

    suspend fun getRandomPrompt(): String? {
        return try {
            val snapshot = db.collection("prompts").get().await()
            if (!snapshot.isEmpty) {
                val prompts = snapshot.documents.mapNotNull { it.getString("prompt") }
                prompts.randomOrNull()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
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

    fun getPostsForCurrentUser(): Flow<List<Post>> = callbackFlow {
        if (userId == "anonymous") {
            trySend(emptyList()).isSuccess
            awaitClose { }
            return@callbackFlow
        }

        val subscription = postsCollection
            .whereEqualTo("userId", userId)
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
        val commentId = postsCollection.document(postId).collection("comments").document().id
        val comment = Comment(
            id = commentId,
            postId = postId,
            userId = userId,
            content = content
        )

        val postRef = postsCollection.document(postId)
        val commentRef = postRef.collection("comments").document(commentId)

        db.runTransaction { transaction ->
            transaction.set(commentRef, comment)
            transaction.update(postRef, "commentCount", FieldValue.increment(1))
        }.await()

        // Realtime DB mirror
        database.child("posts").child(postId).child("comments").child(comment.id).setValue(comment)
        database.child("posts").child(postId).child("commentCount").get().addOnSuccessListener { snapshot ->
            val count = snapshot.getValue(Int::class.java) ?: 0
            database.child("posts").child(postId).child("commentCount").setValue(count + 1)
        }
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

    suspend fun toggleReaction(postId: String, reactionType: String) {
        val postRef = postsCollection.document(postId)
        val reactionRef = postRef.collection("reactions").document(userId)

        db.runTransaction { transaction ->
            val postSnapshot = transaction.get(postRef)
            val reactionSnapshot = transaction.get(reactionRef)

            val post = postSnapshot.toObject(Post::class.java)!!
            val reactions = post.reactions.toMutableMap()

            if (reactionSnapshot.exists()) {
                val existingReaction = reactionSnapshot.toObject(Reaction::class.java)!!
                // Decrement the old reaction count
                reactions[existingReaction.type] = (reactions[existingReaction.type] ?: 1) - 1
                if (reactions[existingReaction.type]!! <= 0) {
                    reactions.remove(existingReaction.type)
                }
                transaction.delete(reactionRef)

                if (existingReaction.type != reactionType) {
                    // If the user is changing their reaction, add the new one
                    reactions[reactionType] = (reactions[reactionType] ?: 0) + 1
                    val newReaction = Reaction(id = userId, postId = postId, userId = userId, type = reactionType)
                    transaction.set(reactionRef, newReaction)
                }
            } else {
                // If the user has no existing reaction, add the new one
                reactions[reactionType] = (reactions[reactionType] ?: 0) + 1
                val newReaction = Reaction(id = userId, postId = postId, userId = userId, type = reactionType)
                transaction.set(reactionRef, newReaction)
            }
            transaction.update(postRef, "reactions", reactions)

            // Mirror to Realtime Database
            database.child("posts").child(postId).child("reactions").setValue(reactions)
        }.await()
    }

    suspend fun getUserReaction(postId: String): String? {
        if (userId == "unknown" || userId == "anonymous") return null

        val reactionDoc = postsCollection.document(postId)
            .collection("reactions")
            .document(userId)
            .get()
            .await()

        return if (reactionDoc.exists()) reactionDoc.getString("type") else null
    }

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

    suspend fun updatePost(postId: String, newContent: String) {
        postsCollection.document(postId).update(
            "content", newContent,
            "lastEdited", FieldValue.serverTimestamp()
        ).await()

        // Update in Realtime DB
        database.child("posts").child(postId).child("content").setValue(newContent)
    }

    suspend fun updateComment(postId: String, commentId: String, newContent: String) {
        postsCollection.document(postId).collection("comments").document(commentId).update(
            "content", newContent,
            "lastEdited", FieldValue.serverTimestamp()
        ).await()

        // Update in Realtime DB
        database.child("posts").child(postId).child("comments").child(commentId).child("content")
            .setValue(newContent)
    }

    suspend fun reportPost(postId: String) {
        postsCollection.document(postId).update("reportCount", FieldValue.increment(1)).await()

        // Optional: Mirror in Realtime DB
        database.child("posts").child(postId).child("reportCount")
            .get().addOnSuccessListener {
                val count = it.getValue(Int::class.java) ?: 0
                database.child("posts").child(postId).child("reportCount").setValue(count + 1)
            }
    }

    suspend fun reportComment(postId: String, commentId: String) {
        postsCollection.document(postId).collection("comments").document(commentId)
            .update("reportCount", FieldValue.increment(1)).await()

        database.child("posts").child(postId).child("comments").child(commentId)
            .child("reportCount").get().addOnSuccessListener {
                val count = it.getValue(Int::class.java) ?: 0
                database.child("posts").child(postId).child("comments").child(commentId)
                    .child("reportCount").setValue(count + 1)
            }
    }

    suspend fun deletePost(postId: String) {
        db.runBatch { batch ->
            batch.delete(postsCollection.document(postId))
        }.await()

        // Realtime DB deletion
        database.child("posts").child(postId).removeValue()
    }
}