package com.example.calview.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calview.core.data.repository.GroupsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class JoinGroupUiState(
    val inviteCode: String = "",
    val isJoining: Boolean = false,
    val errorResId: Int? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class JoinGroupViewModel @Inject constructor(
    private val groupsRepository: GroupsRepository,
    private val savedStateHandle: androidx.lifecycle.SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(JoinGroupUiState())
    val uiState = _uiState.asStateFlow()

    init {
        // Initialize from deep link code if present
        savedStateHandle.get<String>("inviteCode")?.let { code ->
            onInviteCodeChanged(code)
        }
    }

    fun onInviteCodeChanged(code: String) {
        if (code.length <= 6) {
            val capitalizedCode = code.uppercase()
            _uiState.update { it.copy(inviteCode = capitalizedCode, errorResId = null) }
            savedStateHandle["inviteCode"] = capitalizedCode
        }
    }

    fun joinGroup() {
        val code = _uiState.value.inviteCode
        if (code.length < 6) {
            _uiState.update { it.copy(errorResId = R.string.invite_code_length_error) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isJoining = true, errorResId = null) }
            val success = groupsRepository.joinGroup(code)
            if (success) {
                _uiState.update { it.copy(isJoining = false, isSuccess = true) }
            } else {
                _uiState.update { it.copy(isJoining = false, errorResId = R.string.join_generic_error) }
            }
        }
    }
}
