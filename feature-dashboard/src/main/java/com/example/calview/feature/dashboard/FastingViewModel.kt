package com.example.calview.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calview.core.data.local.FastingSessionEntity
import com.example.calview.core.data.repository.FastingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.calview.feature.dashboard.worker.FastCompletionWorker
import com.example.calview.core.data.notification.NotificationHandler
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class FastingUiState(
    val isActive: Boolean = false,
    val currentSession: FastingSessionEntity? = null,
    val elapsedMinutes: Int = 0,
    val remainingMinutes: Int = 0,
    val progress: Float = 0f,
    val selectedFastType: FastType = FastType.SIXTEEN_EIGHT,
    val completedFasts: Int = 0,
    val currentStreak: Int = 0,
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
    private val fastingRepository: FastingRepository,
    private val notificationHandler: NotificationHandler,
    private val workManager: WorkManager
) : ViewModel() {
    
    companion object {
        private const val FAST_WORK_TAG = "fast_completion_work"
    }
    
    private val _uiState = MutableStateFlow(FastingUiState())
    val uiState: StateFlow<FastingUiState> = _uiState.asStateFlow()
    
    init {
        // Observe active fast and run timer only when active
        viewModelScope.launch {
            fastingRepository.activeFast.collectLatest { session ->
                _uiState.update { it.copy(
                    isActive = session != null,
                    currentSession = session
                )}
                
                // If there is an active session, run the timer
                if (session != null) {
                    while (true) {
                        updateElapsedTime()
                        delay(1000)
                    }
                }
            }
        }
        
        // Observe completed count
        viewModelScope.launch {
            fastingRepository.completedFastCount.collect { count ->
                _uiState.update { it.copy(completedFasts = count) }
            }
        }

        // Observe current streak
        viewModelScope.launch {
            fastingRepository.currentStreak
                .distinctUntilChanged()
                .collect { streak ->
                    val previousStreak = _uiState.value.currentStreak
                    if (streak > previousStreak && streak > 0) {
                        notificationHandler.showNotification(
                            id = NotificationHandler.ID_FASTING,
                            channelId = NotificationHandler.CHANNEL_ENGAGEMENT,
                            title = "🔥 Fasting Streak!",
                            message = "Amazing! You've reached a $streak-day fasting streak. Keep it up!",
                            navigateTo = "fasting"
                        )
                    }
                    _uiState.update { it.copy(currentStreak = streak) }
                }
        }
        
        // Observe recent sessions
        viewModelScope.launch {
            fastingRepository.getCompletedSessions(5).collect { sessions ->
                _uiState.update { it.copy(recentSessions = sessions) }
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
            
            // Schedule notification
            val request = OneTimeWorkRequestBuilder<FastCompletionWorker>()
                .setInitialDelay(type.targetMinutes.toLong(), TimeUnit.MINUTES)
                .addTag(FAST_WORK_TAG)
                .build()
            workManager.enqueue(request)

            notificationHandler.showNotification(
                id = NotificationHandler.ID_FASTING,
                channelId = NotificationHandler.CHANNEL_SYSTEM,
                title = "⏱️ Fast Started",
                message = "Your ${type.label} fast has begun. Good luck!",
                navigateTo = "fasting"
            )
        }
    }
    
    fun endFast() {
        viewModelScope.launch {
            val state = _uiState.value
            val isCompleted = state.elapsedMinutes >= (state.currentSession?.targetDurationMinutes ?: 0)
            fastingRepository.endFast(completed = isCompleted)
            
            // Cancel scheduled notification
            workManager.cancelAllWorkByTag(FAST_WORK_TAG)

            if (isCompleted) {
                notificationHandler.showNotification(
                    id = NotificationHandler.ID_FASTING,
                    channelId = NotificationHandler.CHANNEL_ENGAGEMENT,
                    title = "🎉 Fast Completed!",
                    message = "Congratulations! You've successfully completed your ${state.currentSession?.fastingType} fast.",
                    navigateTo = "fasting"
                )
            }
        }
    }
    
    fun cancelFast() {
        viewModelScope.launch {
            fastingRepository.cancelFast()
            // Cancel scheduled notification
            workManager.cancelAllWorkByTag(FAST_WORK_TAG)
        }
    }
}
