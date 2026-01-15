package com.example.calview

import com.example.calview.core.data.local.WeightHistoryDao
import com.example.calview.core.data.local.WeightHistoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for WeightHistoryDao operations
 */
class WeightHistoryDaoTest {

    private lateinit var mockDao: FakeWeightHistoryDao
    
    @Before
    fun setup() {
        mockDao = FakeWeightHistoryDao()
    }
    
    @Test
    fun `insertWeight adds new entry`() = runTest {
        val entry = WeightHistoryEntity(weight = 75.5f, timestamp = System.currentTimeMillis())
        
        mockDao.insertWeight(entry)
        
        assertEquals(1, mockDao.getCount())
    }
    
    @Test
    fun `getLatestWeight returns most recent entry`() = runTest {
        val entry1 = WeightHistoryEntity(weight = 75.5f, timestamp = 1000L)
        val entry2 = WeightHistoryEntity(weight = 74.0f, timestamp = 2000L)
        
        mockDao.insertWeight(entry1)
        mockDao.insertWeight(entry2)
        
        val latest = mockDao.getLatestWeight()
        assertEquals(74.0f, latest?.weight)
    }
    
    @Test
    fun `getCount returns correct number of entries`() = runTest {
        mockDao.insertWeight(WeightHistoryEntity(weight = 75.0f))
        mockDao.insertWeight(WeightHistoryEntity(weight = 74.5f))
        mockDao.insertWeight(WeightHistoryEntity(weight = 74.0f))
        
        assertEquals(3, mockDao.getCount())
    }
    
    @Test
    fun `deleteAll removes all entries`() = runTest {
        mockDao.insertWeight(WeightHistoryEntity(weight = 75.0f))
        mockDao.insertWeight(WeightHistoryEntity(weight = 74.5f))
        
        mockDao.deleteAll()
        
        assertEquals(0, mockDao.getCount())
    }
}

/**
 * Fake implementation of WeightHistoryDao for testing
 */
class FakeWeightHistoryDao : WeightHistoryDao {
    private val entries = mutableListOf<WeightHistoryEntity>()
    private var nextId = 1L
    
    override suspend fun insertWeight(entry: WeightHistoryEntity): Long {
        val newEntry = entry.copy(id = nextId++)
        entries.add(newEntry)
        return newEntry.id
    }
    
    override fun getAllWeightHistory(): Flow<List<WeightHistoryEntity>> {
        return flowOf(entries.sortedByDescending { it.timestamp })
    }
    
    override fun getWeightHistorySince(startTime: Long): Flow<List<WeightHistoryEntity>> {
        return flowOf(entries.filter { it.timestamp >= startTime }.sortedByDescending { it.timestamp })
    }
    
    override suspend fun getLatestWeight(): WeightHistoryEntity? {
        return entries.maxByOrNull { it.timestamp }
    }
    
    override fun getWeightHistoryBetween(startTime: Long, endTime: Long): Flow<List<WeightHistoryEntity>> {
        return flowOf(entries.filter { it.timestamp in startTime..endTime }.sortedBy { it.timestamp })
    }
    
    override suspend fun deleteAll() {
        entries.clear()
    }
    
    override suspend fun getCount(): Int {
        return entries.size
    }
}
