package com.example.calview.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.calview.core.data.repository.AuthRepository
import com.example.calview.core.data.repository.UserPreferencesRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Background worker that syncs user data to Firestore daily.
 * Uses WorkManager to run periodically even when app is not in foreground.
 */
@HiltWorker
class DailySyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val authRepository: AuthRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "🔄 DailySyncWorker: Starting daily sync...")
        
        // Check if user is signed in
        val userId = authRepository.getUserId()
        if (userId.isEmpty()) {
            Log.d(TAG, "⏭️ DailySyncWorker: User not signed in, skipping sync")
            return Result.success()
        }
        
        return try {
            Log.d(TAG, "📤 DailySyncWorker: Syncing data for user: $userId")
            userPreferencesRepository.syncToCloud()
            Log.d(TAG, "✅ DailySyncWorker: Daily sync completed successfully")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "❌ DailySyncWorker: Sync failed", e)
            // Retry if network error, otherwise fail
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    companion object {
        const val TAG = "DailySyncWorker"
        const val WORK_NAME = "daily_sync_work"
    }
}
