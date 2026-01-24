package com.example.calview.core.data.repository

import com.example.calview.core.data.local.WeightHistoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing weight history with cloud sync.
 */
interface WeightHistoryRepository {
    fun getAllWeightHistory(): Flow<List<WeightHistoryEntity>>
    fun getWeightHistorySince(startTime: Long): Flow<List<WeightHistoryEntity>>
    suspend fun getLatestWeight(): WeightHistoryEntity?
    suspend fun insertWeight(entry: WeightHistoryEntity): Long
    suspend fun deleteWeight(entry: WeightHistoryEntity)
    suspend fun deleteAll()
    suspend fun restoreFromCloud(): Boolean
}
