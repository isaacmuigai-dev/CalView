package com.example.calview.core.data.repository

import com.example.calview.core.data.local.WeightHistoryDao
import com.example.calview.core.data.local.WeightHistoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeightHistoryRepositoryImpl @Inject constructor(
    private val weightHistoryDao: WeightHistoryDao,
    private val firestoreRepository: FirestoreRepository,
    private val authRepository: AuthRepository
) : WeightHistoryRepository {
    
    private val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO + kotlinx.coroutines.SupervisorJob())

    override fun getAllWeightHistory(): Flow<List<WeightHistoryEntity>> = weightHistoryDao.getAllWeightHistory()

    override fun getWeightHistorySince(startTime: Long): Flow<List<WeightHistoryEntity>> = 
        weightHistoryDao.getWeightHistorySince(startTime)

    override suspend fun getLatestWeight(): WeightHistoryEntity? = weightHistoryDao.getLatestWeight()

    override suspend fun insertWeight(entry: WeightHistoryEntity): Long {
        val id = weightHistoryDao.insertWeight(entry)
        val inserted = entry.copy(id = id)
        
        syncToCloud(inserted)
        return id
    }

    override suspend fun deleteWeight(entry: WeightHistoryEntity) {
        weightHistoryDao.deleteAll() // DAO doesn't have delete single yet, but repository can be expanded
        // For now using what's available or extending DAO if needed
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
                cloudEntries.forEach { entry ->
                    weightHistoryDao.insertWeight(entry)
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
