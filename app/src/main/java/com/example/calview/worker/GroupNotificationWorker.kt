package com.example.calview.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.calview.MainActivity
import com.example.calview.R
import com.example.calview.core.data.notification.NotificationHandler
import com.example.calview.core.data.repository.GroupsRepository
import com.example.calview.core.data.repository.UserPreferencesRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

/**
 * WorkManager worker for group notifications.
 * Checks for new messages, replies, likes, and member joins since last check.
 * Premium feature only (groups require premium).
 */
@HiltWorker
class GroupNotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val groupsRepository: GroupsRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val notificationHandler: NotificationHandler // Added NotificationHandler
) : CoroutineWorker(context, params) {
    
    companion object {
        const val WORK_NAME = "group_notification_work"
        
        // SharedPrefs keys
        private const val PREFS_NAME = "group_notification_prefs"
        private const val PREF_LAST_MESSAGE_CHECK = "last_message_check"
        private const val PREF_LAST_LIKE_CHECK = "last_like_check"
        private const val PREF_LAST_MEMBER_CHECK = "last_member_check"
        private const val PREF_NOTIFIED_IDS = "notified_message_ids"
        
        private const val TAG = "GroupNotificationWorker"
    }
    
    override suspend fun doWork(): Result {
        // Check if user is in any group
        val hasGroup = userPreferencesRepository.isGroupCreated.first()
        if (!hasGroup) {
            Log.d(TAG, "User not in any group, skipping notification check")
            return Result.success()
        }
        
        val prefs = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentTime = System.currentTimeMillis()
        
        // Default to 15 minutes ago if first run
        val defaultTime = currentTime - (15 * 60 * 1000)
        
        try {
            // 1. Check for new messages
            val lastMessageCheck = prefs.getLong(PREF_LAST_MESSAGE_CHECK, defaultTime)
            val notifiedIds = prefs.getStringSet(PREF_NOTIFIED_IDS, emptySet())?.toMutableSet() ?: mutableSetOf()
            
            val newMessages = groupsRepository.getRecentMessagesForNotification(lastMessageCheck)
                .filter { it.id !in notifiedIds }
            
            if (newMessages.isNotEmpty()) {
                Log.d(TAG, "Found ${newMessages.size} new messages")
                
                // Check for replies to user's messages
                val userId = groupsRepository.getCurrentUserId()
                val replies = newMessages.filter { it.replyToId != null && it.senderId != userId }
                val regularMessages = newMessages.filter { it.replyToId == null }
                
                // Show reply notifications first (higher priority)
                replies.forEach { reply ->
                    showReplyNotification(reply.senderName, reply.text.take(50))
                    notifiedIds.add(reply.id)
                }
                
                // Group regular messages by sender for cleaner notifications
                if (regularMessages.isNotEmpty()) {
                    val firstMessage = regularMessages.first()
                    if (regularMessages.size == 1) {
                        showMessageNotification(firstMessage.senderName, firstMessage.text.take(100))
                    } else {
                        showGroupedMessageNotification(regularMessages.size, firstSenderName = firstMessage.senderName)
                    }
                    regularMessages.forEach { notifiedIds.add(it.id) }
                }
            }
            
            prefs.edit()
                .putLong(PREF_LAST_MESSAGE_CHECK, currentTime)
                .putStringSet(PREF_NOTIFIED_IDS, notifiedIds.toList().takeLast(200).toSet()) // Keep last 200
                .apply()
            
            // 2. Check for new likes on user's messages
            val lastLikeCheck = prefs.getLong(PREF_LAST_LIKE_CHECK, defaultTime)
            val recentLikes = groupsRepository.getRecentLikesOnUserMessages(lastLikeCheck)
            
            if (recentLikes.isNotEmpty()) {
                Log.d(TAG, "Found ${recentLikes.size} new likes")
                if (recentLikes.size == 1) {
                    val (messageText, likerName) = recentLikes.first()
                    showLikeNotification(likerName, messageText)
                } else {
                    showGroupedLikeNotification(recentLikes.size)
                }
            }
            
            prefs.edit().putLong(PREF_LAST_LIKE_CHECK, currentTime).apply()
            
            // 3. Check for new member joins
            val lastMemberCheck = prefs.getLong(PREF_LAST_MEMBER_CHECK, defaultTime)
            val newMembers = groupsRepository.getRecentMemberJoins(lastMemberCheck)
            
            if (newMembers.isNotEmpty()) {
                Log.d(TAG, "Found ${newMembers.size} new members")
                val (groupName, memberName) = newMembers.first()
                if (newMembers.size == 1) {
                    showMemberJoinNotification(memberName, groupName)
                } else {
                    showGroupedMemberNotification(newMembers.size, groupName)
                }
            }
            
            prefs.edit().putLong(PREF_LAST_MEMBER_CHECK, currentTime).apply()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error checking group notifications", e)
            return Result.success() // Don't retry, just skip this cycle
        }
        
        return Result.success()
    }
    
    private fun showMessageNotification(senderName: String, messageText: String) {
        notificationHandler.showNotification(
            id = NotificationHandler.ID_GROUP_MESSAGE,
            channelId = NotificationHandler.CHANNEL_SOCIAL,
            title = "\uD83D\uDCAC $senderName",
            message = messageText,
            navigateTo = "groups"
        )
    }
    
    private fun showGroupedMessageNotification(count: Int, firstSenderName: String) {
        notificationHandler.showNotification(
            id = NotificationHandler.ID_GROUP_MESSAGE,
            channelId = NotificationHandler.CHANNEL_SOCIAL,
            title = "\uD83D\uDCAC $count new messages",
            message = "From $firstSenderName and others",
            navigateTo = "groups"
        )
    }
    
    private fun showReplyNotification(senderName: String, replyText: String) {
        notificationHandler.showNotification(
            id = NotificationHandler.ID_GROUP_REPLY + senderName.hashCode() % 100,
            channelId = NotificationHandler.CHANNEL_SOCIAL,
            title = "↩️ $senderName replied to you",
            message = replyText,
            navigateTo = "groups"
        )
    }
    
    private fun showLikeNotification(likerName: String, messageText: String) {
        notificationHandler.showNotification(
            id = NotificationHandler.ID_GROUP_LIKE,
            channelId = NotificationHandler.CHANNEL_SOCIAL,
            title = "❤️ $likerName liked your message",
            message = "\"$messageText...\"",
            navigateTo = "groups"
        )
    }
    
    private fun showGroupedLikeNotification(count: Int) {
        notificationHandler.showNotification(
            id = NotificationHandler.ID_GROUP_LIKE,
            channelId = NotificationHandler.CHANNEL_SOCIAL,
            title = "❤️ $count new likes",
            message = "People are loving your messages!",
            navigateTo = "groups"
        )
    }
    
    private fun showMemberJoinNotification(memberName: String, groupName: String) {
        notificationHandler.showNotification(
            id = NotificationHandler.ID_GROUP_MEMBER,
            channelId = NotificationHandler.CHANNEL_SOCIAL,
            title = "👋 New member!",
            message = "$memberName joined $groupName",
            navigateTo = "groups"
        )
    }
    
    private fun showGroupedMemberNotification(count: Int, groupName: String) {
        notificationHandler.showNotification(
            id = NotificationHandler.ID_GROUP_MEMBER,
            channelId = NotificationHandler.CHANNEL_SOCIAL,
            title = "👋 $count new members!",
            message = "People are joining $groupName",
            navigateTo = "groups"
        )
    }
}
