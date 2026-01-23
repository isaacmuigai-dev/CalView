package com.example.calview.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calview.core.data.repository.AuthRepository
import com.example.calview.core.data.repository.UserPreferencesRepository
import com.example.calview.core.data.repository.UserPreferencesRepositoryImpl
import com.example.calview.core.data.repository.WaterReminderRepository

import com.example.calview.core.data.local.WaterReminderSettingsEntity
import com.example.calview.core.data.local.StreakFreezeEntity
import com.example.calview.core.data.repository.StreakFreezeRepository
import java.time.LocalDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val userName: String = "",
    val photoUrl: String = "",
    val age: Int = 0,
    val referralCode: String = "",
    val userEmail: String = "",
    val userId: String = "",
    // Preferences
    val appearanceMode: String = "automatic",
    val addCaloriesBack: Boolean = false,
    val rolloverCalories: Boolean = false,
    val widgetDarkTheme: Boolean = false,
    // Water Reminder
    val waterReminderEnabled: Boolean = false,
    val waterReminderIntervalHours: Int = 2,
    val waterReminderStartHour: Int = 8,
    val waterReminderEndHour: Int = 22,
    val waterReminderDailyGoalMl: Int = 2500,

    val isPremium: Boolean = true, // TODO: Connect to BillingManager
    // Streak Freeze
    val remainingFreezes: Int = 2,
    val maxFreezes: Int = 2,
    val yesterdayMissed: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val authRepository: AuthRepository,

    private val waterReminderRepository: WaterReminderRepository,
    private val streakFreezeRepository: StreakFreezeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // Collect user profile data and preferences
            combine(
                userPreferencesRepository.userName,
                userPreferencesRepository.photoUrl,
                userPreferencesRepository.age,
                userPreferencesRepository.referralCode,
                userPreferencesRepository.appearanceMode
            ) { userName, photoUrl, age, referralCode, appearanceMode ->
                // Store intermediate result
                IntermediateState(userName, photoUrl, age, referralCode, appearanceMode)
            }.combine(
                combine(
                    userPreferencesRepository.addCaloriesBack,
                    userPreferencesRepository.rolloverExtraCalories,
                    userPreferencesRepository.widgetDarkTheme
                ) { addCaloriesBack, rolloverCalories, widgetDarkTheme ->
                    Triple(addCaloriesBack, rolloverCalories, widgetDarkTheme)
                }
            ) { intermediate: IntermediateState, prefs: Triple<Boolean, Boolean, Boolean> ->
                Pair(intermediate, prefs)
            }.combine(
                waterReminderRepository.observeSettings()
            ) { pair: Pair<IntermediateState, Triple<Boolean, Boolean, Boolean>>, waterSettings: WaterReminderSettingsEntity ->
                val intermediate = pair.first
                val prefs = pair.second
                val addCaloriesBack = prefs.first
                val rolloverCalories = prefs.second
                val widgetDarkTheme = prefs.third
                
                // Generate referral code if not exists
                val code = if (intermediate.referralCode.isEmpty()) {
                    val newCode = UserPreferencesRepositoryImpl.generateReferralCode()
                    userPreferencesRepository.setReferralCode(newCode)
                    newCode
                } else {
                    intermediate.referralCode
                }
                
                SettingsUiState(
                    userName = intermediate.userName,
                    photoUrl = intermediate.photoUrl,
                    age = intermediate.age,
                    referralCode = code,
                    userEmail = authRepository.getUserEmail(),
                    userId = authRepository.getUserId().ifEmpty { code },
                    appearanceMode = intermediate.appearanceMode,
                    addCaloriesBack = addCaloriesBack,
                    rolloverCalories = rolloverCalories,
                    widgetDarkTheme = widgetDarkTheme,
                    waterReminderEnabled = waterSettings.enabled,
                    waterReminderIntervalHours = waterSettings.intervalHours,
                    waterReminderStartHour = waterSettings.startHour,
                    waterReminderEndHour = waterSettings.endHour,
                    waterReminderDailyGoalMl = waterSettings.dailyGoalMl
                )

            }.combine(
                streakFreezeRepository.observeCurrentMonthFreezes()
            ) { uiState: SettingsUiState, freeze: StreakFreezeEntity? ->
                uiState.copy(
                    remainingFreezes = freeze?.let { it.maxFreezes - it.freezesUsed } ?: 2,
                    maxFreezes = freeze?.maxFreezes ?: 2
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    private data class IntermediateState(
        val userName: String,
        val photoUrl: String,
        val age: Int,
        val referralCode: String,
        val appearanceMode: String
    )

    fun updateUserName(name: String) {
        viewModelScope.launch {
            userPreferencesRepository.setUserName(name)
        }
    }

    fun setAppearanceMode(mode: String) {
        viewModelScope.launch {
            userPreferencesRepository.setAppearanceMode(mode)
        }
    }

    fun setAddCaloriesBack(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setAddCaloriesBack(enabled)
        }
    }

    fun setRolloverCalories(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setRolloverExtraCalories(enabled)
        }
    }

    fun useFreeze() {
        viewModelScope.launch {
            // Default behavior is to freeze yesterday if missed
            streakFreezeRepository.useFreeze(LocalDate.now().minusDays(1))
            
            // Refresh state to update UI
            val freeze = streakFreezeRepository.observeCurrentMonthFreezes().firstOrNull()
            _uiState.update { currentState ->
                currentState.copy(
                    remainingFreezes = freeze?.let { it.maxFreezes - it.freezesUsed } ?: 2,
                    maxFreezes = freeze?.maxFreezes ?: 2
                )
            }
        }
    }

    fun setWidgetDarkTheme(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setWidgetDarkTheme(enabled)
        }
    }

    fun setWaterReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            waterReminderRepository.setEnabled(enabled)
        }
    }

    fun setWaterReminderInterval(hours: Int) {
        viewModelScope.launch {
            waterReminderRepository.setInterval(hours)
        }
    }

    fun setWaterReminderDailyGoal(ml: Int) {
        viewModelScope.launch {
            waterReminderRepository.setDailyGoal(ml)
        }
    }
}

