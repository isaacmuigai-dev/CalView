package com.example.calview.core.data.repository

import com.example.calview.core.data.local.FastingDao
import com.example.calview.core.data.local.FastingSessionEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing fasting sessions.
 */
interface FastingRepository {
    val activeFast: Flow<FastingSessionEntity?>
    val allSessions: Flow<List<FastingSessionEntity>>
    val completedFastCount: Flow<Int>
    
    suspend fun startFast(targetDurationMinutes: Int, fastingType: String): Long
    suspend fun endFast(completed: Boolean = true)
    suspend fun cancelFast()
    suspend fun getActiveFast(): FastingSessionEntity?
    fun getCompletedSessions(limit: Int = 10): Flow<List<FastingSessionEntity>>
    suspend fun deleteSession(session: FastingSessionEntity)
    suspend fun deleteAllSessions()
}

@Singleton
class FastingRepositoryImpl @Inject constructor(
    private val fastingDao: FastingDao
) : FastingRepository {
    
    override val activeFast: Flow<FastingSessionEntity?> = fastingDao.getActiveFast()
    
    override val allSessions: Flow<List<FastingSessionEntity>> = fastingDao.getAllSessions()
    
    override val completedFastCount: Flow<Int> = fastingDao.getCompletedFastCount()
    
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
        return fastingDao.insertSession(session)
    }
    
    override suspend fun endFast(completed: Boolean) {
        fastingDao.getActiveFastOnce()?.let { session ->
            fastingDao.updateSession(session.copy(
                endTime = System.currentTimeMillis(),
                isCompleted = completed
            ))
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
}
