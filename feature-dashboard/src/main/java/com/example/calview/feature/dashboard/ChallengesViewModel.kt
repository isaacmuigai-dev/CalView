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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

data class ChallengeUiModel(
    val challenge: ChallengeEntity,
    val participantsCount: String
)

data class ChallengesUiState(
    val activeChallenges: List<ChallengeUiModel> = emptyList(),
    val allBadges: List<BadgeEntity> = emptyList(),
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
                gamificationRepository.activeChallenges.map { challenges ->
                    challenges.map { challenge ->
                        // Simulate social proof: deterministically random based on ID
                        val seed = challenge.id.hashCode().toLong()
                        val random = Random(seed)
                        val count = random.nextInt(1200, 8500)
                        val formattedCount = if (count > 1000) "${String.format("%.1f", count/1000.0)}k" else count.toString()
                        
                        ChallengeUiModel(challenge, formattedCount)
                    }
                }.collectLatest { uiModels ->
                    _uiState.update { it.copy(activeChallenges = uiModels, isLoading = false) }
                }
            }
            
            launch {
                // Use allBadges (locked + unlocked) instead of just unlocked
                gamificationRepository.allBadges.collectLatest { badges ->
                    _uiState.update { it.copy(allBadges = badges) }
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
