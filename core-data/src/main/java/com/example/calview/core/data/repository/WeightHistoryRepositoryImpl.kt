package com.example.calview.core.data.repository

import com.example.calview.core.data.local.WeightHistoryDao
import com.example.calview.core.data.local.WeightHistoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.example.calview.core.data.notification.NotificationHandler
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeightHistoryRepositoryImpl @Inject constructor(
    private val weightHistoryDao: WeightHistoryDao,
    private val firestoreRepository: FirestoreRepository,
    private val authRepository: AuthRepository,
    private val userPreferencesRepository: javax.inject.Provider<UserPreferencesRepository>,
    private val notificationHandler: NotificationHandler
) : WeightHistoryRepository {
    
    private val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO + kotlinx.coroutines.SupervisorJob())

    override fun getAllWeightHistory(): Flow<List<WeightHistoryEntity>> = weightHistoryDao.getAllWeightHistory()

    override fun getWeightHistorySince(startTime: Long): Flow<List<WeightHistoryEntity>> = 
        weightHistoryDao.getWeightHistorySince(startTime)

    override suspend fun getLatestWeight(): WeightHistoryEntity? = weightHistoryDao.getLatestWeight()

    override suspend fun insertWeight(entry: WeightHistoryEntity): Long {
        // Calculate start and end of the day for the given timestamp
        val date = java.time.Instant.ofEpochMilli(entry.timestamp)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate()
        val startTime = date.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endTime = date.plusDays(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli() - 1

        val existingEntry = weightHistoryDao.getWeightEntryInRange(startTime, endTime)
        
        val entryToInsert = if (existingEntry != null) {
            existingEntry.copy(weight = entry.weight, timestamp = entry.timestamp)
        } else {
            entry
        }

        val id = weightHistoryDao.insertWeight(entryToInsert)
        val finalEntry = entryToInsert.copy(id = id)
        
        syncToCloud(finalEntry)
        checkGoalAchievement(entry.weight)
        return id
    }
    
    private fun checkGoalAchievement(currentWeight: Float) {
        scope.launch {
            val prefs = userPreferencesRepository.get()
            val goalWeight = prefs.goalWeight.first()
            val startWeight = prefs.startWeight.first()
            val userGoal = prefs.userGoal.first()
            
            if (goalWeight > 0) {
                val lastNotified = prefs.lastNotifiedGoalWeight.first()
                if (lastNotified == goalWeight) return@launch // Already notified for this goal

                val reached = if (userGoal.contains("Lose", true)) {
                    currentWeight <= goalWeight
                } else if (userGoal.contains("Gain", true)) {
                    currentWeight >= goalWeight
                } else {
                    false
                }
                
                if (reached) {
                    prefs.setLastNotifiedGoalWeight(goalWeight)
                    notificationHandler.showNotification(
                        id = NotificationHandler.ID_WEIGHT_GOAL,
                        channelId = NotificationHandler.CHANNEL_ENGAGEMENT,
                        title = "🎊 Goal Reached!",
                        message = "Incredible! You've reached your target weight of $goalWeight kg. What an achievement!",
                        navigateTo = "progress"
                    )
                }
            }
        }
    }

    override suspend fun deleteWeight(entry: WeightHistoryEntity) {
        weightHistoryDao.deleteWeight(entry)
    }

    override suspend fun deleteAll() {
        weightHistoryDao.deleteAll()
    }

    private fun syncToCloud(entry: WeightHistoryEntity) {
        val userId = authRepository.getUserId()
        if (userId.isNotEmpty()) {
            scope.launch {
                try {
                    firestoreRepository.saveWeightEntry(userId, entry)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override suspend fun restoreFromCloud(): Boolean {
        val userId = authRepository.getUserId()
        if (userId.isEmpty()) return false
        
        return try {
            val cloudEntries = firestoreRepository.getWeightHistory(userId)
            if (cloudEntries.isNotEmpty()) {
                val localHistory = weightHistoryDao.getAllWeightHistory().first()
                val localFirestoreIds = localHistory.map { it.firestoreId }.toSet()
                
                cloudEntries.forEach { entry ->
                    if (!localFirestoreIds.contains(entry.firestoreId)) {
                        weightHistoryDao.insertWeight(entry)
                    }
                }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
