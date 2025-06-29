package com.example.soulvent.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soulvent.data.PostRepository
import com.example.soulvent.model.Comment
import com.example.soulvent.model.Post
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PostViewModel(val repository: PostRepository = PostRepository()) : ViewModel() {

    // --- State for the unfiltered list of all posts ---
    private val _allPosts = MutableStateFlow<List<Post>>(emptyList())

    // --- State for the mood filter ---
    private val _selectedMoodFilter = MutableStateFlow<String?>(null)
    val selectedMoodFilter: StateFlow<String?> = _selectedMoodFilter.asStateFlow()

    // --- State for the list of blocked users ---
    private val _blockedUsers = MutableStateFlow<List<String>>(emptyList())
    val blockedUsers: StateFlow<List<String>> = _blockedUsers.asStateFlow()

    // --- State for the current user's ID ---
    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()


    // --- Public State that combines all data for the final post list ---
    val posts: StateFlow<List<Post>> = combine(
        _allPosts,
        _selectedMoodFilter,
        _blockedUsers
    ) { allPosts, moodFilter, blockedList ->
        val postsFilteredByMood = if (moodFilter != null) {
            allPosts.filter { it.mood == moodFilter }
        } else {
            allPosts
        }
        postsFilteredByMood.filter { post -> post.userId !in blockedList }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Other UI States ---
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _loadError = MutableStateFlow<String?>(null)
    val loadError: StateFlow<String?> = _loadError.asStateFlow()

    private val _isAddingPost = MutableStateFlow(false)
    val isAddingPost: StateFlow<Boolean> = _isAddingPost.asStateFlow()

    private val _addPostError = MutableStateFlow<String?>(null)
    val addPostError: StateFlow<String?> = _addPostError.asStateFlow()

    private val _commentsForSelectedPostSource = MutableStateFlow<List<Comment>>(emptyList())
    val commentsForSelectedPost: StateFlow<List<Comment>> = _commentsForSelectedPostSource
        .combine(_blockedUsers) { comments, blockedList ->
            comments.filter { comment -> comment.userId !in blockedList }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isCommentsLoading = MutableStateFlow(false)
    val isCommentsLoading: StateFlow<Boolean> = _isCommentsLoading.asStateFlow()

    private val _commentsLoadError = MutableStateFlow<String?>(null)
    val commentsLoadError: StateFlow<String?> = _commentsLoadError.asStateFlow()

    private val _isAddingComment = MutableStateFlow(false)
    val isAddingComment: StateFlow<Boolean> = _isAddingComment.asStateFlow()

    private val _addCommentError = MutableStateFlow<String?>(null)
    val addCommentError: StateFlow<String?> = _addCommentError.asStateFlow()


    private val TAG = "PostViewModel"

    init {
        _currentUserId.value = repository.getCurrentUserId()
        listenForBlockedUsers()
        loadPosts()
    }

    private fun listenForBlockedUsers() {
        _currentUserId.value?.let { userId ->
            viewModelScope.launch {
                repository.getBlockedUsersFlow(userId)
                    .catch { e -> Log.e(TAG, "Error listening for blocked users", e) }
                    .collect { blockedList ->
                        _blockedUsers.value = blockedList
                    }
            }
        }
    }

    fun loadPosts() {
        _loadError.value = null
        _isLoading.value = true
        viewModelScope.launch {
            repository.getPosts()
                .catch { e ->
                    Log.e(TAG, "Error collecting posts: ${e.message}", e)
                    _loadError.value = e.message
                    _allPosts.value = emptyList()
                }
                .collect { posts ->
                    _allPosts.value = posts
                    _isLoading.value = false
                }
        }
    }

    fun setMoodFilter(mood: String?) {
        _selectedMoodFilter.value = mood
    }

    fun addPost(content: String, mood: String, onComplete: () -> Unit) {
        _isAddingPost.value = true
        _addPostError.value = null
        viewModelScope.launch {
            try {
                repository.addPost(content, mood)
                onComplete()
            } catch (e: Exception) {
                Log.e(TAG, "addPost: Error adding post: ${e.message}", e)
                _addPostError.value = e.message
            } finally {
                _isAddingPost.value = false
            }
        }
    }

    fun blockUser(userIdToBlock: String) {
        viewModelScope.launch {
            try {
                repository.blockUser(userIdToBlock)
            } catch (e: Exception) {
                Log.e(TAG, "Error blocking user: ${e.message}", e)
            }
        }
    }

    fun toggleLike(postId: String) {
        viewModelScope.launch {
            try {
                repository.toggleLike(postId)
            } catch (e: Exception) {
                Log.e(TAG, "toggleLike: Error toggling like for post $postId: ${e.message}", e)
            }
        }
    }

    suspend fun hasUserLikedPost(postId: String): Boolean {
        return repository.hasUserLikedPost(postId)
    }

    fun loadCommentsForPost(postId: String) {
        _isCommentsLoading.value = true
        _commentsLoadError.value = null
        viewModelScope.launch {
            repository.getCommentsForPost(postId)
                .catch { e ->
                    Log.e(TAG, "Error collecting comments for post $postId: ${e.message}", e)
                    _commentsLoadError.value = e.message
                    _commentsForSelectedPostSource.value = emptyList()
                }
                .collect { comments ->
                    _commentsForSelectedPostSource.value = comments
                    _isCommentsLoading.value = false
                }
        }
    }

    fun addComment(postId: String, content: String, onComplete: () -> Unit) {
        _isAddingComment.value = true
        _addCommentError.value = null
        viewModelScope.launch {
            try {
                repository.addComment(postId, content)
                loadCommentsForPost(postId)
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