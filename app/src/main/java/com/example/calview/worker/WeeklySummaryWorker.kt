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
import com.example.calview.core.data.local.AnalysisStatus
import com.example.calview.core.data.repository.MealRepository
import com.example.calview.core.data.repository.UserPreferencesRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

/**
 * Worker that sends a weekly progress summary on Sunday mornings.
 * Includes calories averaged, days logged, and streak info.
 * Available for all users.
 */
@HiltWorker
class WeeklySummaryWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val mealRepository: MealRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : CoroutineWorker(context, params) {
    
    companion object {
        const val WORK_NAME = "weekly_summary_work"
        const val CHANNEL_ID = "weekly_summary"
        const val NOTIFICATION_ID = 8001
        
        // SharedPrefs key for tracking last summary week
        const val PREF_LAST_SUMMARY_WEEK = "last_summary_week"
    }
    
    override suspend fun doWork(): Result {
        try {
            // Only run on Sundays
            if (LocalDate.now().dayOfWeek != DayOfWeek.SUNDAY) {
                return Result.success()
            }
            
            // Check if we already sent a summary this week
            val prefs = applicationContext.getSharedPreferences("summary_prefs", Context.MODE_PRIVATE)
            val lastSummaryWeek = prefs.getString(PREF_LAST_SUMMARY_WEEK, "")
            val thisWeekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toString()
            
            if (lastSummaryWeek == thisWeekStart) {
                return Result.success() // Already sent this week's summary
            }
            
            // Calculate this week's stats
            val weekStart = LocalDate.now().minusDays(7)
            val allMeals = mealRepository.getAllMeals().first()
                .filter { it.analysisStatus == AnalysisStatus.COMPLETED }
            
            val weeklyMeals = allMeals.filter { meal ->
                val mealDate = java.time.Instant.ofEpochMilli(meal.timestamp)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                mealDate.isAfter(weekStart)
            }
            
            if (weeklyMeals.isEmpty()) {
                // No meals logged this week, send re-engagement instead
                val userName = userPreferencesRepository.userName.first().ifEmpty { "there" }
                    .split(" ").firstOrNull() ?: "there"
                showNoActivityNotification(userName)
            } else {
                // Calculate weekly stats
                val daysLogged = weeklyMeals.map { meal ->
                    java.time.Instant.ofEpochMilli(meal.timestamp)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                }.distinct().count()
                
                val totalCalories = weeklyMeals.sumOf { it.calories }
                val avgCalories = totalCalories / daysLogged.coerceAtLeast(1)
                
                val totalProtein = weeklyMeals.sumOf { it.protein }
                val avgProtein = totalProtein / daysLogged.coerceAtLeast(1)
                
                val userName = userPreferencesRepository.userName.first().ifEmpty { "there" }
                    .split(" ").firstOrNull() ?: "there"
                
                showWeeklySummaryNotification(userName, daysLogged, avgCalories, avgProtein)
            }
            
            // Mark this week as notified
            prefs.edit().putString(PREF_LAST_SUMMARY_WEEK, thisWeekStart).apply()
            
        } catch (e: Exception) {
            android.util.Log.e("WeeklySummaryWorker", "Error generating summary", e)
        }
        
        return Result.success()
    }
    
    private fun showWeeklySummaryNotification(
        userName: String,
        daysLogged: Int,
        avgCalories: Int,
        avgProtein: Int
    ) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        createNotificationChannel(notificationManager)
        
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "main?tab=1") // Progress tab
        }
        
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val consistencyEmoji = when {
            daysLogged >= 7 -> "🏆"
            daysLogged >= 5 -> "⭐"
            daysLogged >= 3 -> "👍"
            else -> "💪"
        }
        
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("$consistencyEmoji Your Weekly Summary")
            .setContentText("You logged meals on $daysLogged days this week!")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Hey $userName! Here's your week:\n" +
                        "📅 $daysLogged days logged\n" +
                        "🔥 Avg $avgCalories calories/day\n" +
                        "💪 Avg ${avgProtein}g protein/day\n\n" +
                        "Keep up the great work this week!"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    private fun showNoActivityNotification(userName: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        createNotificationChannel(notificationManager)
        
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "main?tab=0&showScanMenu=true")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 1, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("📊 Start Fresh This Week!")
            .setContentText("Hey $userName, no meals logged last week")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Hey $userName! You didn't log any meals last week. " +
                        "Start fresh today - just 3 minutes to scan your breakfast " +
                        "and begin tracking your nutrition journey!"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID + 1, notification)
    }
    
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Weekly Summary",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Weekly nutrition progress summaries"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
}
