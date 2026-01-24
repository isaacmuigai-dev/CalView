package com.example.calview.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calview.core.data.repository.*
import com.example.calview.core.data.local.WaterReminderSettingsEntity
import com.example.calview.core.data.local.StreakFreezeEntity
import java.time.LocalDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log

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
    val yesterdayMissed: Boolean = false,
    
    // Account Deletion State
    val isDeletingAccount: Boolean = false,
    val deletionProgressMessage: String = "",
    val isDeletionComplete: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val authRepository: AuthRepository,
    private val waterReminderRepository: WaterReminderRepository,
    private val streakFreezeRepository: StreakFreezeRepository,
    private val mealRepository: MealRepository,
    private val dailyLogRepository: DailyLogRepository,
    private val firestoreRepository: FirestoreRepository,
    private val fastingRepository: FastingRepository,
    private val weightHistoryRepository: WeightHistoryRepository,
    private val socialChallengeRepository: SocialChallengeRepository,
    private val gamificationRepository: GamificationRepository
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

    fun deleteAccount() {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeletingAccount = true, deletionProgressMessage = "Preparing to delete account...") }
            kotlinx.coroutines.delay(800)
            
            val userId = authRepository.getUserId()
            if (userId.isEmpty()) {
                _uiState.update { it.copy(isDeletingAccount = false, deletionProgressMessage = "Error: User ID not found.") }
                return@launch
            }

            // 1. Delete Firestore Data
            _uiState.update { it.copy(deletionProgressMessage = "Deleting cloud data...") }
            try {
                firestoreRepository.deleteUserData(userId)
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Failed to delete Firestore data", e)
                // Continue with local cleanup
            }
            kotlinx.coroutines.delay(1000)

            // 2. Clear Local Data
            _uiState.update { it.copy(deletionProgressMessage = "Clearing local data and cache...") }
            try {
                mealRepository.clearAllMeals()
                dailyLogRepository.clearAllLogs()
                fastingRepository.deleteAllSessions()
                streakFreezeRepository.clearAllData()
                weightHistoryRepository.deleteAll()
                waterReminderRepository.clearAllData()
                socialChallengeRepository.clearAllData()
                gamificationRepository.clearAllData()
                userPreferencesRepository.clearAllData()
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Failed to clear some local data", e)
            }
            kotlinx.coroutines.delay(1000)

            // 3. Delete Firebase Auth Account
            _uiState.update { it.copy(deletionProgressMessage = "Finalizing account deletion...") }
            kotlinx.coroutines.delay(800)
            val result = authRepository.deleteAccount()
            
            if (result.isSuccess) {
                _uiState.update { it.copy(deletionProgressMessage = "Account deleted successfully. Redirecting...") }
                kotlinx.coroutines.delay(2000)
                _uiState.update { it.copy(isDeletionComplete = true) }
            } else {
                val exception = result.exceptionOrNull()
                _uiState.update { it.copy(isDeletingAccount = false, deletionProgressMessage = "Failed to delete account: ${exception?.message}") }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            // Sync preferences one last time
            userPreferencesRepository.syncToCloud()
            
            // Clear all local data for privacy
            try {
                mealRepository.clearAllMeals()
                dailyLogRepository.clearAllLogs()
                fastingRepository.deleteAllSessions()
                streakFreezeRepository.clearAllData()
                weightHistoryRepository.deleteAll()
                waterReminderRepository.clearAllData()
                socialChallengeRepository.clearAllData()
                gamificationRepository.clearAllData()
                userPreferencesRepository.clearAllData()
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Failed to clear local data during logout", e)
            }
            
            authRepository.signOut()
            _uiState.update { it.copy(deletionProgressMessage = "Logging out...", isDeletingAccount = true) }
            kotlinx.coroutines.delay(1000)
            _uiState.update { it.copy(isDeletionComplete = true) } // Use this flag to trigger navigation
        }
    }
}

