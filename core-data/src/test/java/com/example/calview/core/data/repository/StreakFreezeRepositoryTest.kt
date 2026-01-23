package com.example.calview.core.data.repository

import com.example.calview.core.data.local.StreakFreezeDao
import com.example.calview.core.data.local.StreakFreezeEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Unit tests for StreakFreezeRepository
 */
class StreakFreezeRepositoryTest {
    
    @Mock
    private lateinit var streakFreezeDao: StreakFreezeDao
    
    private lateinit var repository: StreakFreezeRepository
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = StreakFreezeRepository(streakFreezeDao)
    }
    
    @Test
    fun `observeCurrentMonthFreezes returns freeze data`() = runTest {
        // Given
        val currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
        val existingFreeze = StreakFreezeEntity(
            month = currentMonth,
            maxFreezes = 2,
            freezesUsed = 1,
            frozenDates = "2026-01-15"
        )
        whenever(streakFreezeDao.observeFreezeForMonth(currentMonth)).thenReturn(flowOf(existingFreeze))
        
        // When
        val result = repository.observeCurrentMonthFreezes().first()
        
        // Then
        assertNotNull(result)
        assertEquals(1, result!!.freezesUsed)
        assertEquals(2, result.maxFreezes)
    }
    
    @Test
    fun `getRemainingFreezes returns correct count`() = runTest {
        // Given
        val currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
        val freeze = StreakFreezeEntity(
            month = currentMonth,
            maxFreezes = 2,
            freezesUsed = 1,
            frozenDates = ""
        )
        whenever(streakFreezeDao.getFreezeForMonth(currentMonth)).thenReturn(freeze)
        
        // When
        val remaining = repository.getRemainingFreezes()
        
        // Then
        assertEquals(1, remaining)
    }
    
    @Test
    fun `hasFreezesAvailable returns false when all freezes used`() = runTest {
        // Given
        val currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
        val freeze = StreakFreezeEntity(
            month = currentMonth,
            maxFreezes = 2,
            freezesUsed = 2,
            frozenDates = "2026-01-14,2026-01-15"
        )
        whenever(streakFreezeDao.getFreezeForMonth(currentMonth)).thenReturn(freeze)
        
        // When
        val canUse = repository.hasFreezesAvailable()
        
        // Then
        assertFalse(canUse)
    }
    
    @Test
    fun `hasFreezesAvailable returns true when freezes available`() = runTest {
        // Given
        val currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
        val freeze = StreakFreezeEntity(
            month = currentMonth,
            maxFreezes = 2,
            freezesUsed = 0,
            frozenDates = ""
        )
        whenever(streakFreezeDao.getFreezeForMonth(currentMonth)).thenReturn(freeze)
        
        // When
        val canUse = repository.hasFreezesAvailable()
        
        // Then
        assertTrue(canUse)
    }
    
    @Test
    fun `useFreeze for specific date increments freezes used`() = runTest {
        // Given
        val currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
        val yesterday = LocalDate.now().minusDays(1)
        val freeze = StreakFreezeEntity(
            month = currentMonth,
            maxFreezes = 2,
            freezesUsed = 0,
            frozenDates = ""
        )
        whenever(streakFreezeDao.getFreezeForMonth(currentMonth)).thenReturn(freeze)
        
        // When
        val result = repository.useFreeze(yesterday)
        
        // Then
        assertTrue(result)
        verify(streakFreezeDao).useFreeze(any(), any())
    }
    
    @Test
    fun `isDateFrozen returns true for frozen date`() = runTest {
        // Given
        val currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
        val targetDate = LocalDate.now().minusDays(1)
        val dateStr = targetDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
        
        val freeze = StreakFreezeEntity(
            month = currentMonth,
            maxFreezes = 2,
            freezesUsed = 1,
            frozenDates = dateStr
        )
        whenever(streakFreezeDao.getFreezeForMonth(currentMonth)).thenReturn(freeze)
        
        // When
        val isFrozen = repository.isDateFrozen(targetDate)
        
        // Then
        assertTrue(isFrozen)
    }
    
    @Test
    fun `isDateFrozen returns false for non-frozen date`() = runTest {
        // Given
        val currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
        val targetDate = LocalDate.now().minusDays(1)
        val otherDateStr = LocalDate.now().minusDays(2).format(DateTimeFormatter.ISO_LOCAL_DATE)

        val freeze = StreakFreezeEntity(
            month = currentMonth,
            maxFreezes = 2,
            freezesUsed = 1,
            frozenDates = otherDateStr // Not our target date
        )
        whenever(streakFreezeDao.getFreezeForMonth(currentMonth)).thenReturn(freeze)
        
        // When
        val isFrozen = repository.isDateFrozen(targetDate)
        
        // Then
        assertFalse(isFrozen)
    }
}
