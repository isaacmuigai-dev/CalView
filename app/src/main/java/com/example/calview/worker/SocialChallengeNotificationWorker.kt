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
import com.example.calview.core.data.repository.SocialChallengeRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.random.Random

/**
 * WorkManager worker for social challenge notifications.
 * Checks for active challenges and sends reminders/updates.
 * Premium feature only.
 */
@HiltWorker
class SocialChallengeNotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val billingManager: BillingManager,
    private val socialChallengeRepository: SocialChallengeRepository
) : CoroutineWorker(context, params) {
    
    companion object {
        const val WORK_NAME = "social_challenge_notification_work"
        const val CHANNEL_ID = "social_challenge_notifications"
        const val NOTIFICATION_ID_BASE = 5000
        
        // SharedPrefs keys for tracking
        const val PREF_LAST_CHALLENGE_CHECK = "last_challenge_check"
        const val PREF_NOTIFIED_CHALLENGES = "notified_challenges"
    }
    
    override suspend fun doWork(): Result {
        // Premium check
        if (!billingManager.isPremium.first()) {
            return Result.success() // Silently skip for non-premium
        }
        
        try {
            val challenges: List<com.example.calview.core.data.local.SocialChallengeEntity> = 
                socialChallengeRepository.getAllActiveUserChallengesSync()
            
            if (challenges.isEmpty()) {
                return Result.success()
            }
            
            val prefs = applicationContext.getSharedPreferences("challenge_prefs", Context.MODE_PRIVATE)
            val notifiedChallenges = prefs.getStringSet(PREF_NOTIFIED_CHALLENGES, emptySet()) ?: emptySet()
            val newNotifiedSet = notifiedChallenges.toMutableSet()
            
            for (challenge in challenges) {
                val endDate = Instant.ofEpochMilli(challenge.endDate)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                val daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), endDate)
                
                // Notify about challenges ending soon (1-3 days remaining)
                if (daysRemaining in 1..3) {
                    val notifyKey = "${challenge.id}_ending_$daysRemaining"
                    if (!notifiedChallenges.contains(notifyKey)) {
                        showChallengeEndingNotification(
                            challengeTitle = challenge.title,
                            challengeId = challenge.id,
                            daysRemaining = daysRemaining.toInt()
                        )
                        newNotifiedSet.add(notifyKey)
                    }
                }
                
                // Notify about new challenges (created in last 24 hours)
                val createdHoursAgo = ChronoUnit.HOURS.between(
                    Instant.ofEpochMilli(challenge.startDate).atZone(ZoneId.systemDefault()),
                    Instant.now().atZone(ZoneId.systemDefault())
                )
                if (createdHoursAgo <= 24) {
                    val notifyKey = "${challenge.id}_new"
                    if (!notifiedChallenges.contains(notifyKey)) {
                        // This would be for challenges others created that you joined
                        // Skip self-created challenges notification
                        if (challenge.creatorId != socialChallengeRepository.getCurrentUserId()) {
                            showNewChallengeNotification(
                                challengeTitle = challenge.title,
                                challengeId = challenge.id,
                                creatorName = challenge.creatorName
                            )
                            newNotifiedSet.add(notifyKey)
                        }
                    }
                }
            }
            
            // Update notified set
            prefs.edit().putStringSet(PREF_NOTIFIED_CHALLENGES, newNotifiedSet).apply()
            
        } catch (e: Exception) {
            android.util.Log.e("SocialChallengeWorker", "Error checking challenges", e)
            return Result.success() // Don't retry on error, just skip
        }
        
        return Result.success()
    }
    
    private fun showChallengeEndingNotification(challengeTitle: String, challengeId: String, daysRemaining: Int) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        createNotificationChannel(notificationManager)
        
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "challenges")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val daysText = if (daysRemaining == 1) "1 day" else "$daysRemaining days"
        
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("🏁 Challenge Ending Soon!")
            .setContentText("\"$challengeTitle\" ends in $daysText")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Your challenge \"$challengeTitle\" ends in $daysText! Make sure to complete your goals and check the leaderboard."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID_BASE + challengeId.hashCode() % 1000, notification)
    }
    
    private fun showNewChallengeNotification(challengeTitle: String, challengeId: String, creatorName: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        createNotificationChannel(notificationManager)
        
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "challenges")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 1, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("🎯 New Challenge!")
            .setContentText("$creatorName invited you to \"$challengeTitle\"")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("$creatorName has invited you to join \"$challengeTitle\"! Accept the challenge and compete with friends."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID_BASE + challengeId.hashCode() % 1000 + 500, notification)
    }
    
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Social Challenges",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Updates about your social challenges with friends"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
}
