package com.example.calview.core.data.repository

import com.example.calview.core.data.model.GroupDto
import com.example.calview.core.data.model.GroupMemberDto
import com.example.calview.core.data.model.GroupMessageDto
import kotlinx.coroutines.flow.Flow
import java.util.Date

interface GroupsRepository {
    /**
     * Create a new group
     */
    suspend fun createGroup(name: String, description: String, photoUrl: String): String

    /**
     * Join an existing group using an invite code
     */
    suspend fun joinGroup(inviteCode: String): Boolean

    /**
     * Leave a group
     */
    suspend fun leaveGroup(groupId: String): Boolean

    /**
     * Get details of a specific group
     */
    suspend fun getGroupDetails(groupId: String): GroupDto?

    /**
     * Observe the members of a group
     */
    fun observeGroupMembers(groupId: String): Flow<List<GroupMemberDto>>

    /**
     * Observe the groups the user is a member of
     */
    fun observeUserGroups(): Flow<List<GroupDto>>

    /**
     * Restore user's group memberships and associated group data from cloud
     */
    suspend fun restoreUserGroups(): Boolean

    /**
     * Send a message to a group. Supports WhatsApp-style replies.
     */
    suspend fun sendMessage(
        groupId: String, 
        text: String, 
        imageUrl: String? = null,
        replyToId: String? = null
    ): String?

    /**
     * Toggles a like on a message atomically.
     */
    suspend fun toggleLikeMessage(groupId: String, messageId: String): Boolean

    /**
     * Observes the like status of a specific message for the current user.
     */
    fun observeIsMessageLiked(messageId: String): Flow<Boolean>

    /**
     * Observes all message IDs liked by the current user within a specific group.
     * Uses a collection group query for hyperscale efficiency.
     */
    fun observeUserLikes(groupId: String): Flow<Set<String>>

    /**
     * Observe the messages/posts of a group with an optional limit
     */
    fun observeGroupMessages(groupId: String, limit: Int = 20): Flow<List<GroupMessageDto>>

    /**
     * Fetch messages older than a specific timestamp (pagination)
     */
    suspend fun getOlderMessages(groupId: String, beforeTimestamp: Date, limit: Int = 20): List<GroupMessageDto>

    /**
     * Search for a group by invite code without joining
     */
    suspend fun getGroupDetailsByInviteCode(inviteCode: String): GroupDto?

    /**
     * Synchronize user's profile info (name, photo) across all group memberships
     */
    suspend fun syncProfileToGroups(): Boolean

    /**
     * Update user's online presence status
     */
    suspend fun updatePresence(groupId: String, isOnline: Boolean)

    /**
     * Set user's typing status (debounced in ViewModel)
     */
    suspend fun setTypingStatus(groupId: String, isTyping: Boolean)

    /**
     * Observe who is currently typing in the group
     */
    fun observeTypingStatus(groupId: String): Flow<List<String>>

    // ===== Notification Support Methods =====
    
    /**
     * Get messages sent to user's groups since a given timestamp.
     * Used by GroupNotificationWorker.
     */
    suspend fun getRecentMessagesForNotification(sinceTimestamp: Long): List<GroupMessageDto>
    
    /**
     * Get recent likes on the current user's messages.
     * Returns a list of (message text, liker name) pairs.
     */
    suspend fun getRecentLikesOnUserMessages(sinceTimestamp: Long): List<Pair<String, String>>
    
    /**
     * Get recent members who joined user's groups.
     * Returns a list of (group name, member name) pairs.
     */
    suspend fun getRecentMemberJoins(sinceTimestamp: Long): List<Pair<String, String>>
    
    /**
     * Get the current user's ID for filtering notifications.
     */
    fun getCurrentUserId(): String?

    /**
     * Remove user from all groups they are a member of.
     * If user is owner, the group is deleted.
     */
    suspend fun removeUserFromAllGroups(): Boolean

    /**
     * Clear all local group-related preferences.
     */
    suspend fun clearLocalData()
}
