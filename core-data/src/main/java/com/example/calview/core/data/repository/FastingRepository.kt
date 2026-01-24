package com.example.calview.core.data.repository

import com.example.calview.core.data.local.FastingDao
import com.example.calview.core.data.local.FastingSessionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing fasting sessions.
 */
interface FastingRepository {
    val activeFast: Flow<FastingSessionEntity?>
    val allSessions: Flow<List<FastingSessionEntity>>
    val completedFastCount: Flow<Int>
    val currentStreak: Flow<Int>
    
    suspend fun startFast(targetDurationMinutes: Int, fastingType: String): Long
    suspend fun endFast(completed: Boolean = true)
    suspend fun cancelFast()
    suspend fun getActiveFast(): FastingSessionEntity?
    fun getCompletedSessions(limit: Int = 10): Flow<List<FastingSessionEntity>>
    suspend fun deleteSession(session: FastingSessionEntity)
    suspend fun deleteAllSessions()
    suspend fun restoreFromCloud(): Boolean
}

@Singleton
class FastingRepositoryImpl @Inject constructor(
    private val fastingDao: FastingDao,
    private val firestoreRepository: FirestoreRepository,
    private val authRepository: AuthRepository
) : FastingRepository {
    
    private val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO + kotlinx.coroutines.SupervisorJob())
    
    override val activeFast: Flow<FastingSessionEntity?> = fastingDao.getActiveFast()
    
    override val allSessions: Flow<List<FastingSessionEntity>> = fastingDao.getAllSessions()
    
    override val completedFastCount: Flow<Int> = fastingDao.getCompletedFastCount()

    override val currentStreak: Flow<Int> = allSessions.map { sessions ->
        calculateStreak(sessions)
    }

    private fun calculateStreak(sessions: List<FastingSessionEntity>): Int {
        val completedDates = sessions
            .filter { it.isCompleted && it.endTime != null }
            .map { 
                java.time.Instant.ofEpochMilli(it.endTime!!)
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate() 
            }
            .distinct()
            .sortedDescending()

        if (completedDates.isEmpty()) return 0

        var streak = 0
        var currentDay = java.time.LocalDate.now()
        
        // If nothing today, check if yesterday was the last active day
        if (!completedDates.contains(currentDay)) {
            currentDay = currentDay.minusDays(1)
        }

        for (date in completedDates) {
            if (date == currentDay) {
                streak++
                currentDay = currentDay.minusDays(1)
            } else if (date.isBefore(currentDay)) {
                break
            }
        }
        return streak
    }
    
    override suspend fun startFast(targetDurationMinutes: Int, fastingType: String): Long {
        // End any existing fast first
        fastingDao.getActiveFastOnce()?.let { existing ->
            fastingDao.updateSession(existing.copy(
                endTime = System.currentTimeMillis(),
                isCompleted = false
            ))
        }
        
        // Start new fast
        val session = FastingSessionEntity(
            startTime = System.currentTimeMillis(),
            targetDurationMinutes = targetDurationMinutes,
            fastingType = fastingType
        )
        val id = fastingDao.insertSession(session)
        val inserted = session.copy(id = id)
        
        syncSessionToCloud(inserted)
        return id
    }
    
    override suspend fun endFast(completed: Boolean) {
        fastingDao.getActiveFastOnce()?.let { session ->
            val updated = session.copy(
                endTime = System.currentTimeMillis(),
                isCompleted = completed
            )
            fastingDao.updateSession(updated)
            syncSessionToCloud(updated)
        }
    }
    
    private fun syncSessionToCloud(session: FastingSessionEntity) {
        val userId = authRepository.getUserId()
        if (userId.isNotEmpty()) {
            scope.launch {
                try {
                    firestoreRepository.saveFastingSession(userId, session)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
    
    override suspend fun cancelFast() {
        fastingDao.getActiveFastOnce()?.let { session ->
            fastingDao.deleteSession(session)
        }
    }
    
    override suspend fun getActiveFast(): FastingSessionEntity? {
        return fastingDao.getActiveFastOnce()
    }
    
    override fun getCompletedSessions(limit: Int): Flow<List<FastingSessionEntity>> {
        return fastingDao.getCompletedSessions(limit)
    }
    
    override suspend fun deleteSession(session: FastingSessionEntity) {
        fastingDao.deleteSession(session)
    }
    
    override suspend fun deleteAllSessions() {
        fastingDao.deleteAllSessions()
    }

    override suspend fun restoreFromCloud(): Boolean {
        val userId = authRepository.getUserId()
        if (userId.isEmpty()) return false
        
        return try {
            val cloudSessions = firestoreRepository.getFastingSessions(userId)
            if (cloudSessions.isNotEmpty()) {
                cloudSessions.forEach { session ->
                    fastingDao.insertSession(session)
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
