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
import kotlin.random.Random

/**
 * WorkManager worker for social challenge notifications.
 * Checks for new participants, rank changes, and challenge updates.
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
    }
    
    override suspend fun doWork(): Result {
        // Premium check
        if (!billingManager.isPremium.first()) {
            return Result.success() // Silently skip for non-premium
        }
        
        try {
            val userId = socialChallengeRepository.getCurrentUserId() ?: return Result.success()
            val challenges = socialChallengeRepository.observeUserChallenges().first()
            
            for (challenge in challenges) {
                // Check for new participants or rank changes
                // In a real implementation, we would compare with last known state stored in prefs
                // For now, we'll simulate a check for demonstration or rely on server-side push notifications
                // typically handling this via FCM is better, but worker can poll for updates
                
                // This worker is mainly a placeholder for local polling if FCM isn't used
                // Implementation depends on how we track "new" events locally
            }
            
            // Example notification logic (commented out as we need state tracking)
            /*
            val message = "Someone joined your challenge!"
            showNotification(message, challenge.id)
            */
            
        } catch (e: Exception) {
            return Result.failure()
        }
        
        return Result.success()
    }
    
    fun showNotification(message: String, challengeId: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Social Challenges",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Updates about your social challenges"
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "challenges")
            putExtra("challenge_id", challengeId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Use appropriate icon
            .setContentTitle("Challenge Update")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID_BASE + Random.nextInt(1000), notification)
    }
}
