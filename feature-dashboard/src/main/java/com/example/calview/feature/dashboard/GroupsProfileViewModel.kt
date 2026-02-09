package com.example.calview.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calview.core.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class GroupsSetupFlow {
    CREATE, JOIN
}

data class GroupsProfileUiState(
    val firstName: String = "",
    val lastName: String = "",
    val username: String = "",
    val profilePhotoUrl: String = "",
    val customPhotoUri: android.net.Uri? = null, // Added customPhotoUri
    val isUsernameAvailable: Boolean? = null,
    val isCheckingUsername: Boolean = false,
    val selectedInitialColorIndex: Int = 0,
    val setupFlow: GroupsSetupFlow = GroupsSetupFlow.CREATE,
    val isSaving: Boolean = false // Added isSaving
)

@HiltViewModel
class GroupsProfileViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val groupsRepository: com.example.calview.core.data.repository.GroupsRepository,
    private val storageRepository: com.example.calview.core.data.repository.StorageRepository,
    private val savedStateHandle: androidx.lifecycle.SavedStateHandle,
    @param:dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupsProfileUiState())
    val uiState: StateFlow<GroupsProfileUiState> = _uiState.asStateFlow()

    init {
        // Restore from SavedStateHandle if available
        restoreState()

        viewModelScope.launch {
            combine(
                userPreferencesRepository.groupsFirstName.filter { it.isNotEmpty() },
                userPreferencesRepository.groupsLastName.filter { it.isNotEmpty() },
                userPreferencesRepository.groupsUsername.filter { it.isNotEmpty() },
                userPreferencesRepository.groupsProfilePhotoUrl.filter { it.isNotEmpty() }
            ) { firstName, lastName, username, photoUrl ->
                _uiState.update { it.copy(
                    firstName = firstName,
                    lastName = lastName,
                    username = username,
                    profilePhotoUrl = photoUrl
                ) }
            }.collect()
        }
    }

    private fun restoreState() {
        val firstName = savedStateHandle.get<String>("firstName")
        val lastName = savedStateHandle.get<String>("lastName")
        val username = savedStateHandle.get<String>("username")
        val customPhotoUri = savedStateHandle.get<android.net.Uri>("customPhotoUri")
        
        if (firstName != null || lastName != null || username != null || customPhotoUri != null) {
            _uiState.update { it.copy(
                firstName = firstName ?: "",
                lastName = lastName ?: "",
                username = username ?: "",
                customPhotoUri = customPhotoUri
            ) }
        }
    }

    fun updateFirstName(name: String) {
        _uiState.update { it.copy(firstName = name) }
        savedStateHandle["firstName"] = name
    }

    fun updateLastName(name: String) {
        _uiState.update { it.copy(lastName = name) }
        savedStateHandle["lastName"] = name
    }

    fun updateUsername(username: String) {
        _uiState.update { it.copy(username = username, isUsernameAvailable = null) }
        savedStateHandle["username"] = username
        // Simple mock availability check
        if (username.length >= 3) {
            checkUsernameAvailability(username)
        }
    }

    private fun checkUsernameAvailability(username: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCheckingUsername = true) }
            // Simulating network delay
            kotlinx.coroutines.delay(500)
            _uiState.update { it.copy(
                isCheckingUsername = false,
                isUsernameAvailable = true // For now, all usernames are available
            ) }
        }
    }

    fun updateProfilePhotoUrl(url: String) {
        _uiState.update { it.copy(profilePhotoUrl = url, customPhotoUri = null) }
    }

    fun updateCustomPhotoUri(uri: android.net.Uri?) {
        _uiState.update { it.copy(customPhotoUri = uri, profilePhotoUrl = "") }
        savedStateHandle["customPhotoUri"] = uri
    }

    fun updateSelectedColorIndex(index: Int) {
        _uiState.update { it.copy(selectedInitialColorIndex = index) }
    }

    fun setSetupFlow(flow: GroupsSetupFlow) {
        _uiState.update { it.copy(setupFlow = flow) }
    }

    fun saveProfile(onSuccess: () -> Unit = {}) {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            
            var finalPhotoUrl = state.profilePhotoUrl
            var tempFilePath: String? = null
            
            try {
                // Upload custom photo if exists
                state.customPhotoUri?.let { uri ->
                    tempFilePath = copyUriToInternalStorage(uri)
                    tempFilePath?.let { path ->
                        val userId = groupsRepository.getCurrentUserId() ?: "unknown_user"
                        val uploadedUrl = storageRepository.uploadProfilePhoto(path, userId)
                        if (uploadedUrl != null) {
                            finalPhotoUrl = uploadedUrl
                        }
                    }
                }

                userPreferencesRepository.setGroupsFirstName(state.firstName)
                userPreferencesRepository.setGroupsLastName(state.lastName)
                userPreferencesRepository.setGroupsUsername(state.username)
                userPreferencesRepository.setGroupsProfilePhotoUrl(finalPhotoUrl)
                userPreferencesRepository.setGroupsProfileComplete(true)
                
                // Sync current profile to all memberships
                groupsRepository.syncProfileToGroups()
                
                onSuccess()
            } catch (e: Exception) {
                android.util.Log.e("GroupsProfileVM", "Error saving profile", e)
            } finally {
                // Cleanup temp file
                tempFilePath?.let { java.io.File(it).delete() }
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    private fun copyUriToInternalStorage(uri: android.net.Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val file = java.io.File(context.cacheDir, "temp_profile_${System.currentTimeMillis()}.jpg")
            inputStream.use { input ->
                java.io.FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            android.util.Log.e("GroupsProfileVM", "Error copying URI to storage", e)
            null
        }
    }
}
