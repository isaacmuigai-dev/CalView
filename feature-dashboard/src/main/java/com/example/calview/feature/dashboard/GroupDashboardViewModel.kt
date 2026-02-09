package com.example.calview.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calview.core.data.model.GroupDto
import com.example.calview.core.data.model.GroupMessageDto
import com.example.calview.core.data.repository.GroupsRepository
import com.example.calview.core.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GroupDashboardUiState(
    val currentGroup: GroupDto? = null,
    val availableGroups: List<GroupDto> = emptyList(),
    val messages: List<GroupMessageDto> = emptyList(),
    val inputText: String = "",
    val isJoining: Boolean = false,
    val attachedImageUrl: String? = null,
    val selectedReplyMessage: GroupMessageDto? = null,
    val isLoadingOlder: Boolean = false,
    val hasMoreMessages: Boolean = false,
    val typingUsers: List<String> = emptyList(),
    val memberCount: Int = 0,
    val onlineCount: Int = 0,
    val userInitials: String = "",
    val likedMessageIds: Set<String> = emptySet()
)

@HiltViewModel
class GroupDashboardViewModel @Inject constructor(
    private val groupsRepository: GroupsRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val savedStateHandle: androidx.lifecycle.SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupDashboardUiState())
    val uiState: StateFlow<GroupDashboardUiState> = _uiState.asStateFlow()

    private var messageLimit = 20
    private var typingJob: kotlinx.coroutines.Job? = null
    private var messagesJob: kotlinx.coroutines.Job? = null

    init {
        // Restore from SavedStateHandle
        _uiState.update { it.copy(
            inputText = savedStateHandle.get<String>("inputText") ?: "",
            attachedImageUrl = savedStateHandle.get<String>("attachedImageUrl")
        ) }
        
        observeProfile()
        observeGroups()
    }

    private fun observeProfile() {
        viewModelScope.launch {
            combine(
                userPreferencesRepository.groupsFirstName,
                userPreferencesRepository.groupsLastName
            ) { firstName, lastName ->
                if (firstName.isNotEmpty() && lastName.isNotEmpty()) {
                    "${firstName[0]}${lastName[0]}".uppercase()
                } else if (firstName.isNotEmpty()) {
                    firstName.take(2).uppercase()
                } else "U"
            }.collect { initials ->
                _uiState.update { it.copy(userInitials = initials) }
            }
        }
    }

    private fun observeGroups() {
        viewModelScope.launch {
            groupsRepository.observeUserGroups()
                .collect { groups ->
                    _uiState.update { state ->
                        // Maintain current selection if it still exists in the list
                        val nextGroup = if (state.currentGroup != null && groups.any { it.id == state.currentGroup.id }) {
                            state.currentGroup
                        } else {
                            groups.firstOrNull()
                        }
                        
                        // If group changed or was first set
                        if (nextGroup?.id != state.currentGroup?.id) {
                            startObservingGroup(nextGroup)
                        }

                        state.copy(
                            availableGroups = groups,
                            currentGroup = nextGroup
                        )
                    }
                }
        }
    }

    private fun startObservingGroup(group: GroupDto?) {
        messagesJob?.cancel()
        if (group == null) {
            _uiState.update { it.copy(messages = emptyList(), typingUsers = emptyList(), memberCount = 0, onlineCount = 0) }
            return
        }

        messagesJob = viewModelScope.launch {
            // Set presence to online
            groupsRepository.updatePresence(group.id, true)
            
            combine(
                groupsRepository.observeGroupMessages(group.id, messageLimit),
                groupsRepository.observeTypingStatus(group.id),
                groupsRepository.observeGroupMembers(group.id),
                groupsRepository.observeUserLikes(group.id)
            ) { messages, typing, members, likes ->
                StateTuple(messages, typing, members, likes)
            }.collect { (messages, typing, members, likes) ->
                _uiState.update { current ->
                    // If we've switched groups while this was collecting, ignore
                    if (current.currentGroup?.id != group.id) return@update current

                    val mergedMessages = (messages + current.messages)
                        .distinctBy { it.id }
                        .sortedByDescending { it.timestamp }

                    current.copy(
                        messages = mergedMessages,
                        typingUsers = typing,
                        memberCount = members.size,
                        onlineCount = members.count { it.isOnline },
                        likedMessageIds = likes,
                        hasMoreMessages = messages.size >= messageLimit
                    )
                }
            }
        }
    }

    fun switchGroup(group: GroupDto) {
        if (_uiState.value.currentGroup?.id == group.id) return
        
        // Update presence for old group before switching
        val oldGroupId = _uiState.value.currentGroup?.id
        if (oldGroupId != null) {
            viewModelScope.launch {
                groupsRepository.updatePresence(oldGroupId, false)
                groupsRepository.setTypingStatus(oldGroupId, false)
            }
        }

        _uiState.update { it.copy(currentGroup = group, messages = emptyList()) }
        startObservingGroup(group)
    }

    // Helper data class for state observation
    private data class StateTuple(
        val messages: List<GroupMessageDto>,
        val typing: List<String>,
        val members: List<com.example.calview.core.data.model.GroupMemberDto>,
        val likes: Set<String>
    )

    fun onInputTextChanged(text: String) {
        _uiState.update { it.copy(inputText = text) }
        savedStateHandle["inputText"] = text
        
        // Handle typing indicator with debounce
        typingJob?.cancel()
        typingJob = viewModelScope.launch {
            val groupId = _uiState.value.currentGroup?.id ?: return@launch
            groupsRepository.setTypingStatus(groupId, true)
            kotlinx.coroutines.delay(3000) // 3 seconds timeout
            groupsRepository.setTypingStatus(groupId, false)
        }
    }

    fun loadMoreMessages() {
        val state = _uiState.value
        if (state.isLoadingOlder || !state.hasMoreMessages) return
        val groupId = state.currentGroup?.id ?: return
        val oldestTimestamp = state.messages.lastOrNull()?.timestamp ?: return

        _uiState.update { it.copy(isLoadingOlder = true) }
        
        viewModelScope.launch {
            val olderMessages = groupsRepository.getOlderMessages(groupId, oldestTimestamp, 20)
            _uiState.update { current ->
                current.copy(
                    messages = current.messages + olderMessages,
                    isLoadingOlder = false,
                    hasMoreMessages = olderMessages.size >= 20
                )
            }
        }
    }

    fun onImageAttached(url: String?) {
        _uiState.update { it.copy(attachedImageUrl = url) }
        savedStateHandle["attachedImageUrl"] = url
    }

    fun onReplySelected(message: GroupMessageDto) {
        _uiState.update { it.copy(selectedReplyMessage = message) }
    }

    fun cancelReply() {
        _uiState.update { it.copy(selectedReplyMessage = null) }
    }

    fun toggleLikeMessage(messageId: String) {
        val groupId = _uiState.value.currentGroup?.id ?: return
        viewModelScope.launch {
            // The Firestore observeUserLikes listener will automatically update the UI.
            // No optimistic local set manipulation is needed.
            groupsRepository.toggleLikeMessage(groupId, messageId)
        }
    }

    fun sendMessage() {
        val state = _uiState.value
        val groupId = state.currentGroup?.id ?: return
        if (state.inputText.isBlank() && state.attachedImageUrl == null) return

        viewModelScope.launch {
            // Stop typing status immediately when sending
            typingJob?.cancel()
            groupsRepository.setTypingStatus(groupId, false)

            val messageId = groupsRepository.sendMessage(
                groupId = groupId,
                text = state.inputText,
                imageUrl = state.attachedImageUrl,
                replyToId = state.selectedReplyMessage?.id
            )
            if (messageId != null) {
                _uiState.update { it.copy(
                    inputText = "", 
                    attachedImageUrl = null,
                    selectedReplyMessage = null
                ) }
                savedStateHandle["inputText"] = ""
                savedStateHandle["attachedImageUrl"] = null
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Use standalone scope since viewModelScope is cancelled when onCleared() is called
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            kotlinx.coroutines.withContext(kotlinx.coroutines.NonCancellable) {
                val groupId = _uiState.value.currentGroup?.id ?: return@withContext
                groupsRepository.updatePresence(groupId, false)
                groupsRepository.setTypingStatus(groupId, false)
            }
        }
    }
}
