package com.example.calview.core.data.model

import java.util.Date

/**
 * Data Transfer Object for Groups in Firestore
 */
data class GroupDto(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val photoUrl: String = "",
    val creatorId: String = "",
    val createdAt: Date = Date(),
    val inviteCode: String = "",
    val memberCount: Int = 0
)

/**
 * Data Transfer Object for Group Members in Firestore
 */
data class GroupMemberDto(
    val groupId: String = "",
    val userId: String = "",
    val joinedAt: Date = Date(),
    val role: String = "member", // "owner", "member"
    val userName: String = "",
    val userPhotoUrl: String = "",
    val isOnline: Boolean = false
)

/**
 * Data Transfer Object for Group Messages/Posts in Firestore
 */
data class GroupMessageDto(
    val id: String = "",
    val groupId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderPhotoUrl: String = "",
    val text: String = "",
    val imageUrl: String? = null,
    val timestamp: Date = Date(),
    val likeCount: Int = 0,
    val replyToId: String? = null,
    val replyToText: String? = null,
    val replyToSenderName: String? = null
)
