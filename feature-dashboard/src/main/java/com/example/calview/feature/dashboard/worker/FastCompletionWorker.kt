package com.example.calview.feature.dashboard.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.calview.core.data.notification.NotificationHandler
import com.example.calview.core.data.repository.FastingRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Worker to notify user when their fasting target duration is reached.
 */
@HiltWorker
class FastCompletionWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val fastingRepository: FastingRepository,
    private val notificationHandler: NotificationHandler
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val session = fastingRepository.getActiveFast()
        
        if (session != null) {
            val now = System.currentTimeMillis()
            val elapsedMillis = now - session.startTime
            val targetMillis = session.targetDurationMinutes * 60000L
            
            if (elapsedMillis >= targetMillis) {
                notificationHandler.showNotification(
                    id = NotificationHandler.ID_FASTING,
                    channelId = NotificationHandler.CHANNEL_ENGAGEMENT,
                    title = "🎉 Fast Completed!",
                    message = "Congratulations! You've successfully reached your fasting goal of ${session.fastingType}.",
                    navigateTo = "fasting"
                )
                return Result.success()
            }
        }

        return Result.success()
    }
}
