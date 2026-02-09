package com.example.calview.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calview.core.data.model.GroupDto
import com.example.calview.core.data.model.GroupMemberDto
import com.example.calview.core.data.repository.GroupsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GroupSettingsUiState(
    val group: GroupDto? = null,
    val members: List<GroupMemberDto> = emptyList(),
    val isLoading: Boolean = false,
    val leaveSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class GroupSettingsViewModel @Inject constructor(
    private val groupsRepository: GroupsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupSettingsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadActiveGroup()
    }

    private fun loadActiveGroup() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // Observe the first group and switch member observer automatically
            groupsRepository.observeUserGroups()
                .flatMapLatest { groups ->
                    val group = groups.firstOrNull()
                    if (group != null) {
                        _uiState.update { it.copy(group = group) }
                        groupsRepository.observeGroupMembers(group.id)
                    } else {
                        flowOf(emptyList())
                    }
                }
                .collect { members ->
                    _uiState.update { it.copy(members = members, isLoading = false) }
                }
        }
    }

    fun leaveGroup() {
        val groupId = _uiState.value.group?.id ?: return
        viewModelScope.launch {
            val success = groupsRepository.leaveGroup(groupId)
            if (success) {
                _uiState.update { it.copy(leaveSuccess = true) }
            }
        }
    }
}
