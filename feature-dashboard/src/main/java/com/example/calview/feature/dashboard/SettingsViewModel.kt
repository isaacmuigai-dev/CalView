package com.example.calview.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calview.core.data.repository.AuthRepository
import com.example.calview.core.data.repository.UserPreferencesRepository
import com.example.calview.core.data.repository.UserPreferencesRepositoryImpl
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
    val rolloverCalories: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val authRepository: AuthRepository
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
                    userPreferencesRepository.rolloverExtraCalories
                ) { addCaloriesBack, rolloverCalories ->
                    Pair(addCaloriesBack, rolloverCalories)
                }
            ) { intermediate, (addCaloriesBack, rolloverCalories) ->
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
                    rolloverCalories = rolloverCalories
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
}

