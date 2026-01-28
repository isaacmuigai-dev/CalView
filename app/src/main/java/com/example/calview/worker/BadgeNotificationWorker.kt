package com.example.calview.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.calview.MainActivity
import com.example.calview.R
import com.example.calview.core.data.billing.BillingManager
import com.example.calview.core.data.repository.GamificationRepository
import com.example.calview.core.data.repository.UserPreferencesRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import kotlin.random.Random

/**
 * WorkManager worker for badge and achievement notifications.
 * Checks for new badges earned and milestone streaks.
 * Premium feature only.
 */
@HiltWorker
class BadgeNotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val billingManager: BillingManager,
    private val gamificationRepository: GamificationRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : CoroutineWorker(context, params) {
    
    companion object {
        const val WORK_NAME = "badge_notification_work"
        const val CHANNEL_ID = "badge_notifications"
        const val NOTIFICATION_ID_BASE = 6000
        
        // Streak milestones to celebrate
        val STREAK_MILESTONES = listOf(7, 14, 21, 30, 50, 100)
        
        // SharedPrefs key for tracking last notified badge count
        const val PREF_LAST_BADGE_COUNT = "last_badge_count"
        const val PREF_LAST_STREAK_MILESTONE = "last_streak_milestone"
    }
    
    override suspend fun doWork(): Result {
        // Premium check
        if (!billingManager.isPremium.first()) {
            return Result.success() // Silently skip for non-premium
        }
        
        try {
            // Check for new badges
            checkForNewBadges()
            
            // Check for streak milestones (this is a gamification feature)
            checkForStreakMilestones()
            
            // Also trigger the challenge progress check
            gamificationRepository.checkChallengeProgress()
            
        } catch (e: Exception) {
            android.util.Log.e("BadgeNotificationWorker", "Error checking badges", e)
        }
        
        return Result.success()
    }
    
    private suspend fun checkForNewBadges() {
        val badges = gamificationRepository.unlockedBadges.first()
        val currentBadgeCount = badges.size
        
        // Get last known badge count from SharedPreferences
        val prefs = applicationContext.getSharedPreferences("badge_prefs", Context.MODE_PRIVATE)
        val lastBadgeCount = prefs.getInt(PREF_LAST_BADGE_COUNT, 0)
        
        if (currentBadgeCount > lastBadgeCount && lastBadgeCount > 0) {
            // New badge(s) earned since last check
            val newBadges = badges.sortedByDescending { it.dateUnlocked }.take(currentBadgeCount - lastBadgeCount)
            
            for (badge in newBadges) {
                showBadgeNotification(badge.name, badge.description)
            }
        }
        
        // Update stored count
        prefs.edit().putInt(PREF_LAST_BADGE_COUNT, currentBadgeCount).apply()
    }
    
    private suspend fun checkForStreakMilestones() {
        val userLevel = userPreferencesRepository.userLevel.first()
        val userXp = userPreferencesRepository.userXp.first()
        
        // Check for level up notification (XP is close to threshold)
        val xpToNextLevel = userLevel * 1000
        val xpProgress = (userXp.toFloat() / xpToNextLevel * 100).toInt()
        
        if (xpProgress >= 95 && xpProgress < 100) {
            showLevelUpSoonNotification(userLevel, xpToNextLevel - userXp)
        }
    }
    
    private fun showBadgeNotification(badgeName: String, description: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        createNotificationChannel(notificationManager)
        
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "achievements")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("🏆 New Badge Earned!")
            .setContentText("You earned the \"$badgeName\" badge!")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Congratulations! You earned the \"$badgeName\" badge! $description"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID_BASE + Random.nextInt(1000), notification)
    }
    
    private fun showLevelUpSoonNotification(currentLevel: Int, xpNeeded: Int) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        createNotificationChannel(notificationManager)
        
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "main?tab=1") // Progress tab
        }
        
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 1, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("⬆️ Almost Level ${currentLevel + 1}!")
            .setContentText("Just $xpNeeded XP away from leveling up!")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("You're so close to Level ${currentLevel + 1}! Log a meal or hit a goal to earn the last $xpNeeded XP and level up!"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID_BASE + 999, notification)
    }
    
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Achievements",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for earned badges and achievements"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
}
