package com.example.calview.feature.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calview.core.data.repository.AuthRepository
import com.example.calview.core.data.repository.CommentDto
import com.example.calview.core.data.repository.FeatureRequestDto
import com.example.calview.core.data.repository.FirestoreRepository
import com.example.calview.core.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Feedback Hub / Feature Request screen
 * Manages real-time sync with Firestore for feature requests and comments
 */
@HiltViewModel
class FeedbackHubViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository,
    private val authRepository: AuthRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    
    companion object {
        private const val TAG = "FeedbackHubVM"
    }
    
    // Current user info
    private val _currentUserId = MutableStateFlow("")
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()
    
    private val _currentUserName = MutableStateFlow("User")
    val currentUserName: StateFlow<String> = _currentUserName.asStateFlow()
    
    private val _currentUserPhotoUrl = MutableStateFlow("")
    val currentUserPhotoUrl: StateFlow<String> = _currentUserPhotoUrl.asStateFlow()
    
    // Feature requests from Firestore (real-time)
    val featureRequests: StateFlow<List<FeatureRequestDto>> = 
        firestoreRepository.observeFeatureRequests()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    // Currently selected request for comments view
    private val _selectedRequestId = MutableStateFlow<String?>(null)
    val selectedRequestId: StateFlow<String?> = _selectedRequestId.asStateFlow()
    
    // Comments for selected request (real-time)
    private val _comments = MutableStateFlow<List<CommentDto>>(emptyList())
    val comments: StateFlow<List<CommentDto>> = _comments.asStateFlow()
    
    init {
        loadCurrentUser()
    }
    
    private fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                val userId = authRepository.getUserId()
                _currentUserId.value = userId
                
                // Get user name from preferences
                val userName = userPreferencesRepository.userName.first()
                _currentUserName.value = if (userName.isNotEmpty()) userName else "User"
                
                // Get photo URL
                val photoUrl = userPreferencesRepository.photoUrl.first()
                _currentUserPhotoUrl.value = photoUrl
                
                Log.d(TAG, "Loaded user: ${_currentUserName.value} (${_currentUserId.value})")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading current user", e)
            }
        }
    }
    
    /**
     * Post a new feature request
     */
    fun postFeatureRequest(title: String, description: String, tags: List<String>) {
        val userId = _currentUserId.value
        if (userId.isEmpty()) {
            _error.value = "Please sign in to post a feature request"
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val request = FeatureRequestDto(
                    title = title,
                    description = description,
                    authorId = userId,
                    authorName = _currentUserName.value,
                    authorPhotoUrl = _currentUserPhotoUrl.value,
                    votes = 0,
                    votedBy = emptyList(),
                    commentCount = 0,
                    createdAt = System.currentTimeMillis(),
                    status = "OPEN",
                    tags = tags
                )
                firestoreRepository.postFeatureRequest(request)
                Log.d(TAG, "Feature request posted successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error posting feature request", e)
                _error.value = "Failed to post request: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Vote on a feature request (toggle vote)
     */
    fun voteOnRequest(requestId: String) {
        val userId = _currentUserId.value
        if (userId.isEmpty()) {
            _error.value = "Please sign in to vote"
            return
        }
        
        viewModelScope.launch {
            try {
                // Find current vote state
                val request = featureRequests.value.find { it.id == requestId }
                val hasVoted = request?.votedBy?.contains(userId) == true
                
                // Toggle vote
                firestoreRepository.voteOnRequest(requestId, userId, !hasVoted)
                Log.d(TAG, "Vote ${if (!hasVoted) "added" else "removed"} for request $requestId")
            } catch (e: Exception) {
                Log.e(TAG, "Error voting on request", e)
                _error.value = "Failed to vote: ${e.message}"
            }
        }
    }
    
    /**
     * Select a request to view comments (starts real-time observation)
     */
    fun selectRequest(requestId: String?) {
        _selectedRequestId.value = requestId
        
        if (requestId != null) {
            viewModelScope.launch {
                firestoreRepository.observeComments(requestId)
                    .collect { commentsList ->
                        _comments.value = commentsList
                        Log.d(TAG, "Comments updated: ${commentsList.size}")
                    }
            }
        } else {
            _comments.value = emptyList()
        }
    }
    
    /**
     * Post a comment on the currently selected request
     */
    fun postComment(content: String) {
        val requestId = _selectedRequestId.value
        val userId = _currentUserId.value
        
        if (requestId == null) {
            _error.value = "No request selected"
            return
        }
        
        if (userId.isEmpty()) {
            _error.value = "Please sign in to comment"
            return
        }
        
        if (content.isBlank()) {
            return
        }
        
        viewModelScope.launch {
            try {
                val comment = CommentDto(
                    authorId = userId,
                    authorName = _currentUserName.value,
                    authorPhotoUrl = _currentUserPhotoUrl.value,
                    content = content.trim(),
                    createdAt = System.currentTimeMillis(),
                    likes = 0,
                    likedBy = emptyList()
                )
                firestoreRepository.postComment(requestId, comment)
                
                // Update comment count
                val newCount = _comments.value.size + 1
                firestoreRepository.updateCommentCount(requestId, newCount)
                
                Log.d(TAG, "Comment posted successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error posting comment", e)
                _error.value = "Failed to post comment: ${e.message}"
            }
        }
    }
    
    /**
     * Like a comment (toggle like)
     */
    fun likeComment(commentId: String) {
        val requestId = _selectedRequestId.value
        val userId = _currentUserId.value
        
        if (requestId == null || userId.isEmpty()) {
            return
        }
        
        viewModelScope.launch {
            try {
                // Find current like state
                val comment = _comments.value.find { it.id == commentId }
                val hasLiked = comment?.likedBy?.contains(userId) == true
                
                // Toggle like
                firestoreRepository.likeComment(requestId, commentId, userId, !hasLiked)
                Log.d(TAG, "Like ${if (!hasLiked) "added" else "removed"} for comment $commentId")
            } catch (e: Exception) {
                Log.e(TAG, "Error liking comment", e)
            }
        }
    }
    
    /**
     * Check if current user has voted on a request
     */
    fun hasVotedOnRequest(requestId: String): Boolean {
        val userId = _currentUserId.value
        if (userId.isEmpty()) return false
        
        val request = featureRequests.value.find { it.id == requestId }
        return request?.votedBy?.contains(userId) == true
    }
    
    /**
     * Check if current user has liked a comment
     */
    fun hasLikedComment(commentId: String): Boolean {
        val userId = _currentUserId.value
        if (userId.isEmpty()) return false
        
        val comment = _comments.value.find { it.id == commentId }
        return comment?.likedBy?.contains(userId) == true
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _error.value = null
    }
}
