package com.example.soulvent.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.example.soulvent.data.PostRepository
import com.example.soulvent.model.Comment
import com.example.soulvent.model.Post
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow // Added for immutability
import kotlinx.coroutines.flow.catch // Added for error handling in flows
import kotlinx.coroutines.launch
import android.util.Log // For logging

/**
 * ViewModel for managing Post, Comment, and Reaction (Like) data.
 * Handles data loading, adding, and interactions with the PostRepository.
 */
class PostViewModel(private val repository: PostRepository = PostRepository()) : ViewModel() {

    // --- StateFlows for UI Consumption ---

    // Live list of all posts
    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow() // Expose as read-only StateFlow

    // Indicates if the main list of posts is currently being loaded
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error message for loading posts (null if no error)
    private val _loadError = MutableStateFlow<String?>(null)
    val loadError: StateFlow<String?> = _loadError.asStateFlow()

    // Indicates if a new post is currently being added
    private val _isAddingPost = MutableStateFlow(false)
    val isAddingPost: StateFlow<Boolean> = _isAddingPost.asStateFlow()

    // Error message for adding a post (null if no error)
    private val _addPostError = MutableStateFlow<String?>(null)
    val addPostError: StateFlow<String?> = _addPostError.asStateFlow()

    // List of comments for the currently selected post
    private val _commentsForSelectedPost = MutableStateFlow<List<Comment>>(emptyList())
    val commentsForSelectedPost: StateFlow<List<Comment>> = _commentsForSelectedPost.asStateFlow()

    // Indicates if comments for the selected post are currently being loaded
    private val _isCommentsLoading = MutableStateFlow(false)
    val isCommentsLoading: StateFlow<Boolean> = _isCommentsLoading.asStateFlow()

    // Error message for loading comments (null if no error)
    private val _commentsLoadError = MutableStateFlow<String?>(null)
    val commentsLoadError: StateFlow<String?> = _commentsLoadError.asStateFlow()

    // Indicates if a new comment is currently being added
    private val _isAddingComment = MutableStateFlow(false)
    val isAddingComment: StateFlow<Boolean> = _isAddingComment.asStateFlow()

    // Error message for adding a comment (null if no error)
    private val _addCommentError = MutableStateFlow<String?>(null)
    val addCommentError: StateFlow<String?> = _addCommentError.asStateFlow()

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    // It's good practice to get the userId right before using it in an operation,
    // as anonymous auth might complete asynchronously after ViewModel init.
    // However, the PostRepository already handles getting the latest UID internally for each call.
    // Keeping this property for convenience if needed elsewhere in ViewModel.
    private val currentUserId: String? get() = auth.currentUser?.uid

    private val TAG = "PostViewModel" // Tag for logging

    init {
        // Decide if you want to load immediately or on a specific screen's LaunchedEffect.
        // For a home screen displaying posts, loading on init is common.
        // However, if the anonymous auth is not yet complete, these calls might fail initially.
        // The LaunchedEffect in HomeScreen is a good way to ensure it loads when the UI is ready.
        // loadPosts()
    }

    /**
     * Loads all posts from the repository.
     */
    fun loadPosts() {
        // Reset error state before loading
        _loadError.value = null
        _isLoading.value = true
        viewModelScope.launch {
            repository.getPosts()
                .catch { e -> // Catch any exceptions from the flow upstream (e.g., Firestore errors)
                    Log.e(TAG, "Error collecting posts: ${e.message}", e)
                    _loadError.value = e.message // Set user-friendly error message
                    _posts.value = emptyList() // Clear posts or keep old data
                }
                .collect { posts ->
                    _posts.value = posts
                    _isLoading.value = false // Loading finishes whether successful or not
                }
        }
    }

    /**
     * Adds a new post.
     * @param content The content of the post.
     * @param onComplete Callback to execute after the post is added (e.g., navigate back).
     */
    fun addPost(content: String, onComplete: () -> Boolean) {
        _isAddingPost.value = true
        _addPostError.value = null // Reset error state
        viewModelScope.launch {
            try {
                repository.addPost(content)
                // Post added successfully, now refresh the list of posts
                loadPosts() // Trigger a refresh for the home screen
                onComplete() // Callback to navigate or show success message
            } catch (e: Exception) {
                Log.e(TAG, "addPost: Error adding post: ${e.message}", e)
                _addPostError.value = e.message // Set error message for UI
                // Optionally show a Toast here if you prefer immediate feedback for this operation
            } finally {
                _isAddingPost.value = false
            }
        }
    }

    /**
     * Toggles the like status for a given post.
     * @param postId The ID of the post to like/unlike.
     */
    fun toggleLike(postId: String) {
        viewModelScope.launch {
            try {
                repository.toggleLike(postId)
                // No need to explicitly reload all posts here for just a like,
                // as Firestore's snapshot listener in getPosts() should update automatically.
                // If PostItem needs an immediate update, you might pass a callback or specific state.
            } catch (e: Exception) {
                Log.e(TAG, "toggleLike: Error toggling like for post $postId: ${e.message}", e)
                // You could expose a temporary error state for like failures if needed,
                // or just log it and rely on the UI to reflect actual state after next refresh.
            }
        }
    }

    /**
     * Checks if the current user has liked a specific post.
     * @param postId The ID of the post to check.
     * @return True if the user has liked the post, false otherwise.
     */
    suspend fun hasUserLikedPost(postId: String): Boolean {
        return repository.hasUserLikedPost(postId)
    }

    /**
     * Loads comments for a specific post.
     * @param postId The ID of the post to load comments for.
     */
    fun loadCommentsForPost(postId: String) {
        _isCommentsLoading.value = true
        _commentsLoadError.value = null // Reset error state
        viewModelScope.launch {
            repository.getCommentsForPost(postId)
                .catch { e ->
                    Log.e(TAG, "Error collecting comments for post $postId: ${e.message}", e)
                    _commentsLoadError.value = e.message
                    _commentsForSelectedPost.value = emptyList()
                }
                .collect { comments ->
                    _commentsForSelectedPost.value = comments
                    _isCommentsLoading.value = false
                }
        }
    }

    /**
     * Adds a new comment to a specific post.
     * @param postId The ID of the post to add the comment to.
     * @param content The content of the comment.
     * @param onComplete Callback to execute after the comment is added.
     */
    fun addComment(postId: String, content: String, onComplete: () -> Unit) {
        _isAddingComment.value = true
        _addCommentError.value = null // Reset error state
        viewModelScope.launch {
            try {
                repository.addComment(postId, content)
                // Comment added successfully, refresh comments for the post
                // The snapshot listener in getCommentsForPost should automatically update.
                // You might not need to call loadCommentsForPost here directly if the listener is active.
                // But calling it ensures refresh if it wasn't active or for immediate UI feedback.
                // If using a live listener in CommentsScreen, this manual call might be redundant.
                loadCommentsForPost(postId) // Re-fetch comments to show new one and updated count
                onComplete()
            } catch (e: Exception) {
                Log.e(TAG, "addComment: Error adding comment: ${e.message}", e)
                _addCommentError.value = e.message
            } finally {
                _isAddingComment.value = false
            }
        }
    }
}