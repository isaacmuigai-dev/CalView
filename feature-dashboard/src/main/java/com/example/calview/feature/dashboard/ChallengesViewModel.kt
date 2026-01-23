package com.example.calview.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calview.core.data.local.BadgeEntity
import com.example.calview.core.data.local.ChallengeEntity
import com.example.calview.core.data.repository.GamificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChallengesUiState(
    val activeChallenges: List<ChallengeEntity> = emptyList(),
    val unlockedBadges: List<BadgeEntity> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class ChallengesViewModel @Inject constructor(
    private val gamificationRepository: GamificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChallengesUiState())
    val uiState: StateFlow<ChallengesUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            // Initialize if first run
            gamificationRepository.initializeChallenges()
            // Check progress
            gamificationRepository.checkChallengeProgress()
            
            launch {
                gamificationRepository.activeChallenges.collectLatest { challenges ->
                    _uiState.update { it.copy(activeChallenges = challenges, isLoading = false) }
                }
            }
            
            launch {
                gamificationRepository.unlockedBadges.collectLatest { badges ->
                    _uiState.update { it.copy(unlockedBadges = badges) }
                }
            }
        }
    }
    
    fun refresh() {
        viewModelScope.launch {
            gamificationRepository.checkChallengeProgress()
        }
    }
}
