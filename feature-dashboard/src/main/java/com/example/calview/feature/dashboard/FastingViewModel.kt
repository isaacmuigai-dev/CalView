package com.example.calview.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calview.core.data.local.FastingSessionEntity
import com.example.calview.core.data.repository.FastingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FastingUiState(
    val isActive: Boolean = false,
    val currentSession: FastingSessionEntity? = null,
    val elapsedMinutes: Int = 0,
    val remainingMinutes: Int = 0,
    val progress: Float = 0f,
    val selectedFastType: FastType = FastType.SIXTEEN_EIGHT,
    val completedFasts: Int = 0,
    val recentSessions: List<FastingSessionEntity> = emptyList()
)

enum class FastType(val label: String, val fastingHours: Int, val eatingHours: Int) {
    SIXTEEN_EIGHT("16:8", 16, 8),
    EIGHTEEN_SIX("18:6", 18, 6),
    TWENTY_FOUR("20:4", 20, 4),
    OMAD("23:1", 23, 1);
    
    val targetMinutes: Int get() = fastingHours * 60
}

@HiltViewModel
class FastingViewModel @Inject constructor(
    private val fastingRepository: FastingRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(FastingUiState())
    val uiState: StateFlow<FastingUiState> = _uiState.asStateFlow()
    
    init {
        // Observe active fast
        viewModelScope.launch {
            fastingRepository.activeFast.collect { session ->
                _uiState.update { it.copy(
                    isActive = session != null,
                    currentSession = session
                )}
            }
        }
        
        // Observe completed count
        viewModelScope.launch {
            fastingRepository.completedFastCount.collect { count ->
                _uiState.update { it.copy(completedFasts = count) }
            }
        }
        
        // Observe recent sessions
        viewModelScope.launch {
            fastingRepository.getCompletedSessions(5).collect { sessions ->
                _uiState.update { it.copy(recentSessions = sessions) }
            }
        }
        
        // Timer tick - update elapsed time every minute
        viewModelScope.launch {
            while (true) {
                delay(1000) // Update every second for smoother UI
                updateElapsedTime()
            }
        }
    }
    
    private fun updateElapsedTime() {
        val session = _uiState.value.currentSession ?: return
        val now = System.currentTimeMillis()
        val elapsedMillis = now - session.startTime
        val elapsedMinutes = (elapsedMillis / 60000).toInt()
        val targetMinutes = session.targetDurationMinutes
        val remainingMinutes = (targetMinutes - elapsedMinutes).coerceAtLeast(0)
        val progress = (elapsedMinutes.toFloat() / targetMinutes).coerceIn(0f, 1f)
        
        _uiState.update { it.copy(
            elapsedMinutes = elapsedMinutes,
            remainingMinutes = remainingMinutes,
            progress = progress
        )}
    }
    
    fun selectFastType(type: FastType) {
        _uiState.update { it.copy(selectedFastType = type) }
    }
    
    fun startFast() {
        viewModelScope.launch {
            val type = _uiState.value.selectedFastType
            fastingRepository.startFast(
                targetDurationMinutes = type.targetMinutes,
                fastingType = type.label
            )
        }
    }
    
    fun endFast() {
        viewModelScope.launch {
            val state = _uiState.value
            val isCompleted = state.elapsedMinutes >= (state.currentSession?.targetDurationMinutes ?: 0)
            fastingRepository.endFast(completed = isCompleted)
        }
    }
    
    fun cancelFast() {
        viewModelScope.launch {
            fastingRepository.cancelFast()
        }
    }
}
