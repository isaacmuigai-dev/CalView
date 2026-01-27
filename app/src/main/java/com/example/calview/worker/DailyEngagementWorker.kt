package com.example.calview.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.calview.core.data.local.DailyLogDao
import com.example.calview.core.data.repository.UserPreferencesRepository
import com.example.calview.core.data.notification.NotificationHandler
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Worker to check for missing logs in the evening and remind the user.
 * Mentions Streak Freeze as an incentive.
 */
@HiltWorker
class DailyEngagementWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val dailyLogDao: DailyLogDao,
    private val userPrefs: UserPreferencesRepository,
    private val notificationHandler: NotificationHandler
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME = "daily_engagement_work"
        const val REMINDER_HOUR = 20 // 8 PM
    }

    override suspend fun doWork(): Result {
        // Only run in the evening (approx)
        val currentHour = LocalTime.now().hour
        if (currentHour < REMINDER_HOUR) {
            return Result.success()
        }

        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val log = dailyLogDao.getLogForDateSync(today)
        
        // If 0 calories consumed, send a reminder
        val calories = log?.caloriesConsumed ?: 0
        if (calories == 0) {
            val userName = userPrefs.userName.first().ifEmpty { "there" }
            notificationHandler.showNotification(
                id = NotificationHandler.ID_REMINDER,
                channelId = NotificationHandler.CHANNEL_ENGAGEMENT,
                title = "Hey $userName, missing something?",
                message = "You haven't logged any meals today! Log now to save your streak, or use a Streak Freeze if you're busy.",
                navigateTo = "main?tab=0"
            )
        }

        return Result.success()
    }
}
