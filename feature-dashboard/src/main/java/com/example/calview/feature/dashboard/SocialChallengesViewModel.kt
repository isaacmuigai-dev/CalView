package com.example.calview.feature.dashboard

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calview.core.data.local.ChallengeParticipantEntity
import com.example.calview.core.data.local.SocialChallengeEntity
import com.example.calview.core.data.local.SocialChallengeType
import com.example.calview.core.data.repository.SocialChallengeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import android.util.Log
import javax.inject.Inject

data class SocialChallengesUiState(
    val challenges: List<SocialChallengeEntity> = emptyList(),
    val participantsMap: Map<String, List<ChallengeParticipantEntity>> = emptyMap(),
    val currentUserId: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val createdChallenge: SocialChallengeEntity? = null  // For showing invite code dialog
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class SocialChallengesViewModel @Inject constructor(
    private val socialChallengeRepository: SocialChallengeRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SocialChallengesUiState())
    val uiState: StateFlow<SocialChallengesUiState> = _uiState.asStateFlow()
    
    init {
        loadChallenges()
    }
    
    private fun loadChallenges() {
        viewModelScope.launch {
            _uiState.update { it.copy(currentUserId = socialChallengeRepository.getCurrentUserId()) }
            
            socialChallengeRepository.observeUserChallenges()
                .onEach { challenges ->
                    _uiState.update { it.copy(challenges = challenges) }
                }
                .flatMapLatest { challenges ->
                    if (challenges.isEmpty()) {
                        flowOf(emptyMap<String, List<ChallengeParticipantEntity>>())
                    } else {
                        val participantFlows = challenges.map { challenge ->
                            socialChallengeRepository.observeLeaderboard(challenge.id)
                                .map { participants -> challenge.id to participants }
                        }
                        combine(participantFlows) { it.toMap() }
                    }
                }
                .catch { e ->
                    Log.e("SocialChallengesVM", "Error loading challenges: ${e.message}", e)
                    _uiState.update { it.copy(isLoading = false, error = "Failed to sync challenges. Please check your connection.") }
                }
                .collect { participantsMap ->
                    _uiState.update { state ->
                        state.copy(participantsMap = participantsMap)
                    }
                }
        }
    }
    
    fun createChallenge(title: String, type: SocialChallengeType, durationDays: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val userId = socialChallengeRepository.getCurrentUserId()
                if (userId.isNullOrEmpty()) {
                    _uiState.update { it.copy(isLoading = false, error = "You must be signed in to create challenges.") }
                    return@launch
                }

                val challenge = socialChallengeRepository.createChallenge(
                    title = title,
                    description = "",
                    type = type,
                    targetValue = when (type) {
                        SocialChallengeType.STREAK -> durationDays
                        SocialChallengeType.WATER -> 2000 * durationDays
                        SocialChallengeType.STEPS -> 10000 * durationDays
                        else -> durationDays
                    },
                    durationDays = durationDays
                )
                // Store the created challenge so UI can show invite code dialog
                _uiState.update { it.copy(isLoading = false, createdChallenge = challenge) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Failed to create challenge: ${e.message}") }
            }
        }
    }
    
    fun clearCreatedChallenge() {
        _uiState.update { it.copy(createdChallenge = null) }
    }
    
    fun joinChallenge(inviteCode: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = socialChallengeRepository.joinChallenge(inviteCode)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
            )
        }
    }
    
    fun shareChallenge(challenge: SocialChallengeEntity) {
        // This would typically trigger a share intent
        // The actual share happens at the UI layer
    }
    
    fun leaveChallenge(challengeId: String) {
        viewModelScope.launch {
            socialChallengeRepository.leaveChallenge(challengeId)
        }
    }
}
