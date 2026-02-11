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
import java.util.Date

data class GroupSettingsUiState(
    val group: GroupDto? = null,
    val members: List<GroupMemberDto> = emptyList(),
    val isLoading: Boolean = false,
    val leaveSuccess: Boolean = false,
    val error: String? = null
)

import com.example.calview.core.data.repository.UserPreferencesRepository

@HiltViewModel
class GroupSettingsViewModel @Inject constructor(
    private val groupsRepository: GroupsRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupSettingsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadActiveGroup()
    }

    private fun loadActiveGroup() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val userId = groupsRepository.getCurrentUserId()
            
            // Observe the first group and switch member observer automatically
            groupsRepository.observeUserGroups()
                .flatMapLatest { groups ->
                    val group = groups.firstOrNull()
                    if (group != null) {
                        _uiState.update { it.copy(group = group) }
                         // Combine members with local user info to ensure owner presence
                        combine(
                            groupsRepository.observeGroupMembers(group.id),
                            userPreferencesRepository.groupsFirstName,
                            userPreferencesRepository.groupsLastName,
                            userPreferencesRepository.groupsProfilePhotoUrl
                        ) { members, firstName, lastName, photoUrl ->
                             Triple(members, group, Triple(firstName, lastName, photoUrl))
                        }
                    } else {
                        flowOf(Triple(emptyList(), null, Triple("", "", "")))
                    }
                }
                .collect { (members, group, userInfo) ->
                    if (group == null) {
                         _uiState.update { it.copy(members = emptyList(), isLoading = false) }
                         return@collect
                    }

                    val (firstName, lastName, userPhotoUrl) = userInfo
                    val currentUserId = userId ?: ""
                    
                    // Robust check: Is the owner (creator) in the list?
                    // Note: group.creatorId might be null in old data, but usually set.
                    val isOwnerMissing = members.none { it.role == "owner" } // Simplified check
                    
                    val finalMembers = if (isOwnerMissing && group.creatorId == currentUserId) {
                        // Synthesize owner member
                        val ownerMember = GroupMemberDto(
                            userId = currentUserId,
                            groupId = group.id,
                            userName = "$firstName $lastName".trim().ifEmpty { "You" },
                            userPhotoUrl = userPhotoUrl,
                            role = "owner",
                            isOnline = true,
                            joinedAt = Date() // Approximate
                        )
                        members + ownerMember
                    } else {
                        members
                    }

                    val sortedMembers = finalMembers.sortedWith(
                        compareByDescending<GroupMemberDto> { it.role == "owner" }
                            .thenBy { it.joinedAt ?: Date(0) }
                    )
                    _uiState.update { it.copy(members = sortedMembers, isLoading = false) }
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
