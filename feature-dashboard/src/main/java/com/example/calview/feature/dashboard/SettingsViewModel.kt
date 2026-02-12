
package com.example.calview.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calview.core.data.repository.AuthRepository
import com.example.calview.core.data.repository.FirestoreRepository
import com.example.calview.core.data.repository.UserPreferencesRepository
import com.example.calview.core.data.repository.MealRepository
import com.example.calview.core.data.repository.DailyLogRepository
import com.example.calview.core.data.repository.WaterReminderRepository
import com.example.calview.core.data.repository.WeightHistoryRepository
import com.example.calview.core.data.repository.ExerciseRepository
import com.example.calview.core.data.repository.FastingRepository
import com.example.calview.core.data.repository.StreakFreezeRepository
import com.example.calview.core.data.repository.SocialChallengeRepository
import com.example.calview.core.data.repository.GamificationRepository

import com.example.calview.core.data.local.WaterReminderSettingsEntity
import com.example.calview.core.data.local.StreakFreezeEntity
import java.time.LocalDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log
import android.content.Context
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.example.calview.core.data.billing.BillingManager

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
    private val gamificationRepository: GamificationRepository,
    private val exerciseRepository: ExerciseRepository,

    private val billingManager: BillingManager,
    @ApplicationContext private val context: Context
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
                    val newCode = generateReferralCode()
                    userPreferencesRepository.setReferralCode(newCode)
                    newCode
                } else {
                    intermediate.referralCode
                }
                
                val finalUserName = if (intermediate.userName.isBlank()) {
                    authRepository.getCurrentUser()?.displayName ?: "User"
                } else {
                    intermediate.userName
                }

                val finalPhotoUrl = if (intermediate.photoUrl.isBlank()) {
                    authRepository.getCurrentUser()?.photoUrl?.toString() ?: ""
                } else {
                    intermediate.photoUrl
                }
                
                SettingsUiState(
                    userName = finalUserName,
                    photoUrl = finalPhotoUrl,
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
                // Preserve transient flags (deletion status) when applying repo updates
                _uiState.update { current ->
                    state.copy(
                        isDeletingAccount = current.isDeletingAccount,
                        deletionProgressMessage = current.deletionProgressMessage,
                        isDeletionComplete = current.isDeletionComplete
                    )
                }
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

    fun updatePhotoUrl(url: String) {
        viewModelScope.launch {
            userPreferencesRepository.setPhotoUrl(url)

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
        android.util.Log.d("SettingsViewModel", "deleteAccount called")
        viewModelScope.launch {
            _uiState.update { it.copy(isDeletingAccount = true, deletionProgressMessage = "Preparing to delete account...") }
            kotlinx.coroutines.delay(1000)
            
            val userId = authRepository.getUserId()
            android.util.Log.d("SettingsViewModel", "Deleting account for userId: $userId")
            
            if (userId.isEmpty()) {
                android.util.Log.e("SettingsViewModel", "User ID not found")
                _uiState.update { it.copy(isDeletingAccount = false, deletionProgressMessage = "Error: User ID not found.") }
                return@launch
            }

            // 1. Delete Firestore Data (And Storage images)
            _uiState.update { it.copy(deletionProgressMessage = "Deleting cloud data and images...") }
            try {

                android.util.Log.d("SettingsViewModel", "Deleting user data from Firestore...")
                firestoreRepository.deleteUserData(userId)
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Failed to delete Firestore data", e)
                // Continue anyway to ensure account is deleted
            }
            kotlinx.coroutines.delay(1000)

            // 2. Delete Firebase Auth Account (Crucial for sign-out)
            _uiState.update { it.copy(deletionProgressMessage = "Finalizing account deletion...") }
            kotlinx.coroutines.delay(500)
            
            android.util.Log.d("SettingsViewModel", "Deleting Firebase Auth account...")
            val result = authRepository.deleteAccount()
            
            if (!result.isSuccess) {
                val exception = result.exceptionOrNull()
                android.util.Log.e("SettingsViewModel", "Failed to delete auth account", exception)
                
                if (exception is com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException) {
                    _uiState.update { it.copy(isDeletingAccount = false, deletionProgressMessage = "Security Check: Please log in again to delete your account.") }
                    android.util.Log.w("SettingsViewModel", "Recent login required. Prompting user.")
                    // Ideally, we should trigger a re-auth flow here. 
                    // For now, we inform the user they need to re-login.
                    // A better UX would be to emit a side-effect to launch the Google Sign-In intent.
                } else {
                    _uiState.update { it.copy(isDeletingAccount = false, deletionProgressMessage = "Failed to delete account: ${exception?.message}") }
                }
                return@launch
            }
            android.util.Log.d("SettingsViewModel", "Auth account deleted successfully")

            // 3. Cancel background work
            try {
                WorkManager.getInstance(context).cancelAllWork()
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Failed to cancel background work", e)
            }

            // 4. Clear Local Repositories and reset state
            _uiState.update { it.copy(deletionProgressMessage = "Clearing local data and cache...") }
            android.util.Log.d("SettingsViewModel", "Clearing local data...")
            clearAllLocalData()
            billingManager.reset()
            
            kotlinx.coroutines.delay(1000)

            // 5. Show final message
            _uiState.update { it.copy(deletionProgressMessage = "Account deleted successfully. Redirecting...") }
            kotlinx.coroutines.delay(2000)

            // 6. Clear Preferences last (triggers restart)
            userPreferencesRepository.clearAllData()
            
            android.util.Log.d("SettingsViewModel", "Deletion complete. Signaling navigation.")
            _uiState.update { it.copy(isDeletionComplete = true) }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeletingAccount = true, deletionProgressMessage = "Logging out...") }
            
            // 1. Sync preferences one last time
            userPreferencesRepository.syncToCloud()
            
            // 2. Clear all local data
            clearAllLocalData()
            
            // 3. Cancel background work
            try {
                WorkManager.getInstance(context).cancelAllWork()
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Failed to cancel background work", e)
            }

            // 4. Sign out
            authRepository.signOut()
            billingManager.reset()
            kotlinx.coroutines.delay(1000)

            // 5. Clear Preferences last
            userPreferencesRepository.clearAllData()
            
            _uiState.update { it.copy(isDeletionComplete = true) }
        }
    }

    private suspend fun clearAllLocalData() {
        try {
            // Cancel background workers
            // We need a context for WorkManager, but ViewModel doesn't have it directly.
            // However, Repositories usually have @ApplicationContext.
            // For now, let's focus on repository clearing.


            mealRepository.clearAllMeals()
            dailyLogRepository.clearAllLogs()
            fastingRepository.deleteAllSessions()
            streakFreezeRepository.clearAllData()
            weightHistoryRepository.deleteAll()
            waterReminderRepository.clearAllData()
            socialChallengeRepository.clearAllData()
            gamificationRepository.clearAllData()
            exerciseRepository.clearAllExercises()
        } catch (e: Exception) {
            Log.e("SettingsViewModel", "Error in clearAllLocalData", e)
        }
    }

    private fun generateReferralCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6).map { chars[kotlin.random.Random.nextInt(chars.length)] }.joinToString("")
    }
}

