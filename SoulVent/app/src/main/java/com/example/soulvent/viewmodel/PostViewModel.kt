package com.example.soulvent.viewmodel

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soulvent.data.AIArtGenerator
import com.example.soulvent.data.PostRepository
import com.example.soulvent.model.Comment
import com.example.soulvent.model.Post
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.example.soulvent.data.GenerationResult

class PostViewModel(val repository: PostRepository = PostRepository()) : ViewModel() {

    private val _allPosts = MutableStateFlow<List<Post>>(emptyList())
    private val _selectedMoodFilter = MutableStateFlow<String?>(null)
    val selectedMoodFilter: StateFlow<String?> = _selectedMoodFilter.asStateFlow()

    private val _blockedUsers = MutableStateFlow<List<String>>(emptyList())
    val blockedUsers: StateFlow<List<String>> = _blockedUsers.asStateFlow()

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    private val _dailyPrompt = MutableStateFlow<String?>(null)
    val dailyPrompt: StateFlow<String?> = _dailyPrompt.asStateFlow()

    private val _generatedImageBitmap = MutableStateFlow<Bitmap?>(null)
    val generatedImageBitmap = _generatedImageBitmap.asStateFlow()

    private val _isGeneratingImage = MutableStateFlow(false)
    val isGeneratingImage = _isGeneratingImage.asStateFlow()

    private val _imageGenerationError = MutableStateFlow<String?>(null)
    val imageGenerationError = _imageGenerationError.asStateFlow()


    private val _selectedTagFilter = MutableStateFlow<String?>(null)
    val selectedTagFilter: StateFlow<String?> = _selectedTagFilter.asStateFlow()

    val posts: StateFlow<List<Post>> = combine(
        _allPosts,
        _selectedMoodFilter,
        _blockedUsers,
        _selectedTagFilter
    ) { allPosts, moodFilter, blockedList, tagFilter ->
        val postsFilteredByMood = if (moodFilter != null) {
            allPosts.filter { it.mood == moodFilter }
        } else {
            allPosts
        }
        val postsFilteredByTag = if (tagFilter != null) {
            postsFilteredByMood.filter { it.tags.contains(tagFilter) }
        } else {
            postsFilteredByMood
        }
        postsFilteredByTag.filter { post -> post.userId !in blockedList }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _userPosts = MutableStateFlow<List<Post>>(emptyList())
    val userPosts: StateFlow<List<Post>> = _userPosts.asStateFlow()

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
        loadUserPosts()
        loadDailyPrompt()
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

    private fun loadUserPosts() {
        viewModelScope.launch {
            repository.getPostsForCurrentUser()
                .catch { e -> Log.e(TAG, "Error loading user posts", e) }
                .collect { posts ->
                    _userPosts.value = posts
                }
        }
    }

    private fun loadDailyPrompt() {
        viewModelScope.launch {
            _dailyPrompt.value = repository.getRandomPrompt()
        }
    }

    fun generateArtForPost(text: String) {
        viewModelScope.launch {
            _isGeneratingImage.value = true
            _imageGenerationError.value = null
            when (val result = AIArtGenerator.generateImage(text)) {
                is GenerationResult.Success -> {
                    _generatedImageBitmap.value = result.bitmap
                }
                is GenerationResult.Error -> {
                    _imageGenerationError.value = result.message
                }
            }
            _isGeneratingImage.value = false
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
        _selectedTagFilter.value = null
    }

    fun setTagFilter(tag: String?) {
        _selectedTagFilter.value = tag
        _selectedMoodFilter.value = null
    }

    fun addPost(content: String, mood: String, tags: List<String>, onComplete: () -> Unit) {
        _isAddingPost.value = true
        _addPostError.value = null
        viewModelScope.launch {
            try {
                var imageUrl: String? = null
                if (_generatedImageBitmap.value != null) {
                    imageUrl = repository.uploadImage(_generatedImageBitmap.value!!)
                }
                repository.addPost(content, mood, tags, imageUrl)
                onComplete()
            } catch (e: Exception) {
                _addPostError.value = e.message
            } finally {
                _isAddingPost.value = false
                _generatedImageBitmap.value = null
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

    fun toggleReaction(postId: String, reactionType: String) {
        viewModelScope.launch {
            try {
                repository.toggleReaction(postId, reactionType)
            } catch (e: Exception) {
                Log.e(TAG, "toggleReaction: Error toggling reaction for post $postId: ${e.message}", e)
            }
        }
    }

    suspend fun getUserReaction(postId: String): String? {
        return repository.getUserReaction(postId)
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
                onComplete()
            } catch (e: Exception) {
                Log.e(TAG, "addComment: Error adding comment: ${e.message}", e)
                _addCommentError.value = e.message
            } finally {
                _isAddingComment.value = false
            }
        }
    }

    fun editPost(postId: String, newContent: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.updatePost(postId, newContent)
                onComplete()
            } catch (e: Exception) {
                Log.e(TAG, "Error editing post", e)
            }
        }
    }

    fun editComment(postId: String, commentId: String, newContent: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.updateComment(postId, commentId, newContent)
                onComplete()
            } catch (e: Exception) {
                Log.e(TAG, "Error editing comment", e)
            }
        }
    }

    fun reportPost(postId: String) {
        viewModelScope.launch {
            try {
                repository.reportPost(postId)
            } catch (e: Exception) {
                Log.e(TAG, "Error reporting post", e)
            }
        }
    }

    fun reportComment(postId: String, commentId: String) {
        viewModelScope.launch {
            try {
                repository.reportComment(postId, commentId)
            } catch (e: Exception) {
                Log.e(TAG, "Error reporting comment", e)
            }
        }
    }

    fun deletePost(postId: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.deletePost(postId)
                onComplete()
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting post", e)
            }
        }
    }
}