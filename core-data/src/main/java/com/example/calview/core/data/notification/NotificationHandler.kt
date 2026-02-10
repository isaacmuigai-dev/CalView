package com.example.calview.core.data.notification

interface NotificationHandler {
    fun showNotification(
        id: Int,
        channelId: String,
        title: String,
        message: String,
        autoCancel: Boolean = true,
        navigateTo: String? = null
    )

    companion object {
        const val CHANNEL_SYSTEM = "system_notifications"
        const val CHANNEL_ENGAGEMENT = "engagement_notifications"
        const val CHANNEL_SOCIAL = "social_notifications"
        const val CHANNEL_PREMIUM = "premium_notifications"

        const val ID_WEIGHT_GOAL = 1001
        const val ID_BADGE_UNLOCKED = 1002
        const val ID_FASTING = 1003
        const val ID_SOCIAL = 1004
        const val ID_DAILY_GOAL = 1005
        const val ID_PERSONAL_BEST = 1006
        const val ID_PREMIUM = 1007
        const val ID_REMINDER = 1008
        const val ID_FEATURE_REQUEST = 1009
        
        // Groups notifications
        const val ID_GROUP_MESSAGE = 2001
        const val ID_GROUP_REPLY = 2002
        const val ID_GROUP_LIKE = 2003
        const val ID_GROUP_MEMBER = 2004
    }
}
