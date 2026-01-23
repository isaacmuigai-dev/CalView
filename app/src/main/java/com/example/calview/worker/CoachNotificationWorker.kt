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
import com.example.calview.core.data.local.DailyLogDao
import com.example.calview.core.data.repository.UserPreferencesRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

/**
 * WorkManager worker for AI coach notifications.
 * Sends personalized motivational messages based on user's progress.
 * Premium feature only.
 */
@HiltWorker
class CoachNotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val billingManager: BillingManager,
    private val dailyLogDao: DailyLogDao,
    private val userPrefsRepo: UserPreferencesRepository
) : CoroutineWorker(context, params) {
    
    companion object {
        const val WORK_NAME = "coach_notification_work"
        const val CHANNEL_ID = "coach_notifications"
        const val NOTIFICATION_ID = 2001
        
        // Notification types
        const val TYPE_MORNING = "morning"
        const val TYPE_MIDDAY = "midday"
        const val TYPE_EVENING = "evening"
        const val TYPE_STREAK = "streak"
    }
    
    override suspend fun doWork(): Result {
        // Premium check
        if (!billingManager.isPremium.first()) {
            return Result.success() // Silently skip for non-premium
        }
        
        val notificationType = inputData.getString("type") ?: TYPE_MORNING
        val message = generateMessage(notificationType)
        
        if (message.isNotEmpty()) {
            showNotification(message)
        }
        
        return Result.success()
    }
    
    private suspend fun generateMessage(type: String): String {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val log = dailyLogDao.getLogForDateSync(today)
        
        // Get goals from UserPreferencesRepository using Flow.first()
        val calorieGoal = userPrefsRepo.recommendedCalories.first()
        val proteinGoal = userPrefsRepo.recommendedProtein.first()
        
        val consumed = log?.caloriesConsumed ?: 0
        val remaining = calorieGoal - consumed
        val proteinConsumed = log?.proteinConsumed ?: 0
        
        return when (type) {
            TYPE_MORNING -> {
                val morningMessages = listOf(
                    "☀️ Good morning! Your goal today: $calorieGoal cal. Start with a protein-rich breakfast!",
                    "🌅 Rise and shine! ${proteinGoal}g protein awaits. Eggs or yogurt?",
                    "💪 New day, new goals! You've got $calorieGoal calories to fuel your day.",
                    "🎯 Ready to crush it? Log your breakfast and start strong!"
                )
                morningMessages.random()
            }
            
            TYPE_MIDDAY -> {
                when {
                    consumed == 0 -> "🍽️ Haven't logged yet? Open CalView and snap your lunch!"
                    remaining > calorieGoal * 0.6 -> "📊 Only ${consumed} cal logged. You have $remaining left - enjoy a balanced lunch!"
                    proteinConsumed < proteinGoal * 0.4 -> "💪 Need more protein! You're at ${proteinConsumed}g of ${proteinGoal}g goal."
                    else -> "👍 Great progress! $consumed cal logged. Keep it up!"
                }
            }
            
            TYPE_EVENING -> {
                when {
                    consumed == 0 -> "📝 Don't forget to log today's meals before bed!"
                    remaining < 0 -> "⚠️ You're ${-remaining} cal over today. Tomorrow's a new day!"
                    remaining < 300 -> "✨ Almost there! Just $remaining cal left. Finish strong!"
                    else -> "🌙 $remaining cal remaining for dinner. What's on the menu?"
                }
            }
            
            TYPE_STREAK -> {
                // Get streak from shared preferences or calculate
                val streakMessages = listOf(
                    "🔥 Your streak is on fire! Keep logging daily!",
                    "💯 Consistency is key! Another day, another win!",
                    "⭐ You're building amazing habits! Don't stop now!",
                    "🏆 Champions log daily. You're one of them!"
                )
                streakMessages.random()
            }
            
            else -> ""
        }
    }
    
    private fun showNotification(message: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create channel for Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "AI Coach",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Personalized coaching tips and reminders"
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        // Create intent to open app
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("CalView Coach")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID + Random.nextInt(1000), notification)
    }
}
