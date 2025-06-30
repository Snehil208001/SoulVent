package com.example.soulvent.data

import android.graphics.Bitmap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase
import com.example.soulvent.model.Comment
import com.example.soulvent.model.Post
import com.example.soulvent.model.Reaction
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import com.google.firebase.firestore.FieldPath

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
    }

    suspend fun getRandomPrompt(): String? {
        return try {
            val randomId = db.collection("prompts").document().id
            val query = db.collection("prompts")
                .whereGreaterThanOrEqualTo(FieldPath.documentId(), randomId)
                .limit(1)

            var documents = query.get().await()
            if (documents.isEmpty) {
                documents = db.collection("prompts").limit(1).get().await()
            }
            documents.firstOrNull()?.getString("question")
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

    suspend fun toggleReaction(postId: String, reactionType: String) {
        val reactionRef = postsCollection.document(postId)
            .collection("reactions")
            .document(userId)
        val postRef = postsCollection.document(postId)

        db.runTransaction { transaction ->
            val reactionSnapshot = transaction.get(reactionRef)
            val postSnapshot = transaction.get(postRef)
            // Correctly cast the map values to Long
            val currentReactions = postSnapshot.get("reactions") as? Map<String, Long> ?: emptyMap()
            val existingReactionType = reactionSnapshot.getString("type")

            if (reactionSnapshot.exists()) {
                transaction.delete(reactionRef)
                val oldReactionCount = (currentReactions[existingReactionType] ?: 1) - 1
                transaction.update(postRef, "reactions.$existingReactionType", oldReactionCount)

                if (existingReactionType != reactionType) {
                    val newReaction = Reaction(userId, postId, userId, reactionType)
                    transaction.set(reactionRef, newReaction)
                    val newReactionCount = (currentReactions[reactionType] ?: 0) + 1
                    transaction.update(postRef, "reactions.$reactionType", newReactionCount)
                }
            } else {
                val newReaction = Reaction(userId, postId, userId, reactionType)
                transaction.set(reactionRef, newReaction)
                val newReactionCount = (currentReactions[reactionType] ?: 0) + 1
                transaction.update(postRef, "reactions.$reactionType", newReactionCount)
            }
            null
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
    }

    suspend fun updateComment(postId: String, commentId: String, newContent: String) {
        postsCollection.document(postId).collection("comments").document(commentId).update(
            "content", newContent,
            "lastEdited", FieldValue.serverTimestamp()
        ).await()
    }

    suspend fun reportPost(postId: String) {
        postsCollection.document(postId).update("reportCount", FieldValue.increment(1)).await()
    }

    suspend fun reportComment(postId: String, commentId: String) {
        postsCollection.document(postId).collection("comments").document(commentId)
            .update("reportCount", FieldValue.increment(1)).await()
    }

    suspend fun deletePost(postId: String) {
        db.runBatch { batch ->
            batch.delete(postsCollection.document(postId))
        }.await()
    }
}