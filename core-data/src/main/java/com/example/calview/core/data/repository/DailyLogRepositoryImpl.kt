package com.example.calview.core.data.repository

import android.util.Log
import com.example.calview.core.data.local.DailyLogDao
import com.example.calview.core.data.local.DailyLogEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.example.calview.core.data.local.SocialChallengeType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DailyLogRepositoryImpl @Inject constructor(
    private val dailyLogDao: DailyLogDao,
    private val authRepository: AuthRepository,
    private val firestore: FirebaseFirestore,
    private val socialChallengeRepository: SocialChallengeRepository
) : DailyLogRepository {
    
    companion object {
        private const val TAG = "DailyLogRepo"
        private const val USERS_COLLECTION = "users"
        private const val DAILY_LOGS_COLLECTION = "daily_logs"
    }
    
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    
    override fun getLogForDate(date: String): Flow<DailyLogEntity?> {
        return dailyLogDao.getLogForDate(date)
    }
    
    override suspend fun getLogForDateSync(date: String): DailyLogEntity? {
        return dailyLogDao.getLogForDateSync(date)
    }
    
    override fun getAllLogs(): Flow<List<DailyLogEntity>> {
        return dailyLogDao.getAllLogs()
    }
    
    override fun getLogsForRange(startDate: String, endDate: String): Flow<List<DailyLogEntity>> {
        return dailyLogDao.getLogsForRange(startDate, endDate)
    }
    
    override suspend fun saveLog(log: DailyLogEntity) {
        dailyLogDao.insertOrUpdate(log)
        syncLogToCloud(log)
    }
    
    override suspend fun updateSteps(date: String, steps: Int) {
        val existing = dailyLogDao.getLogForDateSync(date)
        if (existing != null) {
            dailyLogDao.updateSteps(date, steps)
            syncLogToCloud(existing.copy(steps = steps))
            socialChallengeRepository.updateUserProgressForType(SocialChallengeType.STEPS, steps)
        } else {
            val newLog = DailyLogEntity(date = date, steps = steps)
            dailyLogDao.insertOrUpdate(newLog)
            syncLogToCloud(newLog)
        }
    }
    
    override suspend fun updateWater(date: String, water: Int) {
        val existing = dailyLogDao.getLogForDateSync(date)
        if (existing != null) {
            dailyLogDao.updateWater(date, water)
            syncLogToCloud(existing.copy(waterIntake = water))
            socialChallengeRepository.updateUserProgressForType(SocialChallengeType.WATER, water)
        } else {
            val newLog = DailyLogEntity(date = date, waterIntake = water)
            dailyLogDao.insertOrUpdate(newLog)
            syncLogToCloud(newLog)
        }
    }
    
    override suspend fun updateWeight(date: String, weight: Float) {
        val existing = dailyLogDao.getLogForDateSync(date)
        if (existing != null) {
            dailyLogDao.updateWeight(date, weight)
            syncLogToCloud(existing.copy(weight = weight))
        } else {
            val newLog = DailyLogEntity(date = date, weight = weight)
            dailyLogDao.insertOrUpdate(newLog)
            syncLogToCloud(newLog)
        }
    }
    
    override suspend fun getOrCreateTodayLog(): DailyLogEntity {
        val today = dateFormat.format(Date())
        val existing = dailyLogDao.getLogForDateSync(today)
        return if (existing != null) {
            existing
        } else {
            val newLog = DailyLogEntity(date = today)
            dailyLogDao.insertOrUpdate(newLog)
            newLog
        }
    }
    
    private fun syncLogToCloud(log: DailyLogEntity) {
        val userId = authRepository.getUserId()
        if (userId.isEmpty()) {
            Log.w(TAG, "Skipping sync - user not signed in")
            return
        }
        
        scope.launch {
            try {
                Log.d(TAG, "Syncing daily log to cloud: ${log.date}")
                firestore.collection(USERS_COLLECTION)
                    .document(userId)
                    .collection(DAILY_LOGS_COLLECTION)
                    .document(log.date)
                    .set(log)
                    .await()
                Log.d(TAG, "Daily log synced successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing daily log", e)
            }
        }
    }
    
    override suspend fun syncToCloud() {
        val userId = authRepository.getUserId()
        if (userId.isEmpty()) return
        
        // This could be improved to only sync unsynced logs
        // For now, we sync on each update
    }
    
    override suspend fun restoreFromCloud(): Boolean {
        val userId = authRepository.getUserId()
        if (userId.isEmpty()) return false
        
        return try {
            val snapshot = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(DAILY_LOGS_COLLECTION)
                .get()
                .await()
            
            val logs = snapshot.toObjects(DailyLogEntity::class.java)
            logs.forEach { log ->
                dailyLogDao.insertOrUpdate(log)
            }
            Log.d(TAG, "Restored ${logs.size} daily logs from cloud")
            logs.isNotEmpty()
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring daily logs", e)
            false
        }
    }

    override suspend fun clearAllLogs() {
        try {
            dailyLogDao.deleteAll()
            Log.d(TAG, "Cleared all local daily logs")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing daily logs", e)
        }
    }
}
