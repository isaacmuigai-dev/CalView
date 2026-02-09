package com.example.calview.feature.dashboard

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import com.example.calview.core.data.repository.GroupsRepository
import com.example.calview.core.data.repository.StorageRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateGroupUiState(
    val groupName: String = "",
    val description: String = "",
    val selectedPhotoIndex: Int = 0,
    val customPhotoUri: Uri? = null,
    val isCreating: Boolean = false,
    val nameSuggestions: List<String> = listOf("Macro Masters", "Snack Trackers", "Fit Feas", "Calorie Crushers", "Protein Pros")
)

@HiltViewModel
class CreateGroupViewModel @Inject constructor(
    private val groupsRepository: GroupsRepository,
    private val storageRepository: StorageRepository,
    private val savedStateHandle: androidx.lifecycle.SavedStateHandle,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateGroupUiState())
    val uiState: StateFlow<CreateGroupUiState> = _uiState.asStateFlow()

    val preGeneratedPhotos = listOf(
        "💪", "🏃", "🏋️", "🧘", "🚴", "🏊", "🧗", "🥊", "🥋", "🤸", // Active
        "🥑", "🥦", "🍎", "🍌", "🍓", "🥗", "🥕", "🌽", "🥒", "🍅", // Healthy Food
        "🔥", "💧", "⚡", "💯", "🏆", "🥇", "🎯", "🚀", "⭐", "👑", // Motivation
        "🍳", "🥩", "🍗", "🥪", "🌮", "🌯", "🍜", "🍣", "🍱", "🍚", // Meals
        "🏀", "⚽", "🏈", "⚾", "🎾", "🏐", "🏉", "🎱", "🏓", "🏸"  // Sports
    )

    init {
        // Restore from SavedStateHandle
        _uiState.update { it.copy(
            groupName = savedStateHandle.get<String>("groupName") ?: "",
            description = savedStateHandle.get<String>("description") ?: "",
            selectedPhotoIndex = savedStateHandle.get<Int>("selectedPhotoIndex") ?: 0,
            customPhotoUri = savedStateHandle.get<Uri>("customPhotoUri")
        ) }
    }

    fun updateGroupName(name: String) {
        _uiState.update { it.copy(groupName = name) }
        savedStateHandle["groupName"] = name
    }

    fun updateDescription(description: String) {
        _uiState.update { it.copy(description = description) }
        savedStateHandle["description"] = description
    }

    fun updateSelectedPhotoIndex(index: Int) {
        _uiState.update { it.copy(selectedPhotoIndex = index, customPhotoUri = null) }
        savedStateHandle["selectedPhotoIndex"] = index
        savedStateHandle["customPhotoUri"] = null
    }

    fun updateCustomPhotoUri(uri: android.net.Uri?) {
        _uiState.update { it.copy(customPhotoUri = uri, selectedPhotoIndex = -1) }
        savedStateHandle["customPhotoUri"] = uri
        savedStateHandle["selectedPhotoIndex"] = -1
    }

    fun createGroup(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.groupName.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isCreating = true) }
            var tempFilePath: String? = null
            try {
                var photoUrl = if (state.selectedPhotoIndex >= 0) {
                    preGeneratedPhotos.getOrNull(state.selectedPhotoIndex) ?: "🤝"
                } else {
                    "🤝"
                }

                // Temporary ID to link photo
                val tempGroupId = java.util.UUID.randomUUID().toString()

                // Upload custom photo if exists
                state.customPhotoUri?.let { uri ->
                    tempFilePath = copyUriToInternalStorage(uri)
                    tempFilePath?.let { path ->
                        val uploadedUrl = storageRepository.uploadGroupPhoto(path, tempGroupId)
                        if (uploadedUrl != null) {
                            photoUrl = uploadedUrl
                        }
                    }
                }

                groupsRepository.createGroup(
                    name = state.groupName.trim(),
                    description = state.description.trim(),
                    photoUrl = photoUrl
                )
                
                // Clear state on success
                savedStateHandle["groupName"] = ""
                savedStateHandle["description"] = ""
                
                onSuccess()
            } catch (e: Exception) {
                android.util.Log.e("CreateGroupVM", "Failed to create group", e)
            } finally {
                // Cleanup temp file
                tempFilePath?.let { java.io.File(it).delete() }
                _uiState.update { it.copy(isCreating = false) }
            }
        }
    }

    private fun copyUriToInternalStorage(uri: android.net.Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val file = java.io.File(context.cacheDir, "temp_group_${System.currentTimeMillis()}.jpg")
            inputStream.use { input ->
                java.io.FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            android.util.Log.e("CreateGroupVM", "Error copying URI to storage", e)
            null
        }
    }
}
