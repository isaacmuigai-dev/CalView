package com.example.calview.core.data.repository

import com.example.calview.core.data.local.WaterReminderDao
import com.example.calview.core.data.local.WaterReminderSettingsEntity
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Unit tests for WaterReminderRepository
 */
class WaterReminderRepositoryTest {
    
    @Mock
    private lateinit var waterReminderDao: WaterReminderDao
    
    private lateinit var repository: WaterReminderRepository
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = WaterReminderRepository(waterReminderDao)
    }
    
    @Test
    fun `getSettings returns saved settings when exists`() = runTest {
        // Given
        val settings = WaterReminderSettingsEntity(
            enabled = true,
            intervalHours = 2,
            startHour = 7,
            endHour = 21,
            dailyGoalMl = 2500
        )
        whenever(waterReminderDao.getSettings()).thenReturn(settings)
        
        // When
        val result = repository.getSettings()
        
        // Then
        assertNotNull(result)
        assertEquals(true, result.enabled)
        assertEquals(2, result.intervalHours)
        assertEquals(2500, result.dailyGoalMl)
    }
    
    @Test
    fun `getSettings returns default when not exists`() = runTest {
        // Given
        whenever(waterReminderDao.getSettings()).thenReturn(null)
        
        // When
        val result = repository.getSettings()
        
        // Then
        assertNotNull(result)
        assertEquals(false, result.enabled)
        assertEquals(2, result.intervalHours)
        assertEquals(2000, result.dailyGoalMl)
    }
    
    @Test
    fun `setEnabled updates settings`() = runTest {
        // Given
        val settings = WaterReminderSettingsEntity(
            enabled = false
        )
        whenever(waterReminderDao.getSettings()).thenReturn(settings)
        
        // When
        repository.setEnabled(true)
        
        // Then
        verify(waterReminderDao).saveSettings(any())
    }
    
    @Test
    fun `setInterval updates interval hours`() = runTest {
        // Given
        val settings = WaterReminderSettingsEntity(
            intervalHours = 2
        )
        whenever(waterReminderDao.getSettings()).thenReturn(settings)
        
        // When
        repository.setInterval(3)
        
        // Then
        verify(waterReminderDao).saveSettings(any())
    }
    
    @Test
    fun `setActiveHours updates start and end hours`() = runTest {
        // Given
        val settings = WaterReminderSettingsEntity(
            startHour = 8,
            endHour = 22
        )
        whenever(waterReminderDao.getSettings()).thenReturn(settings)
        
        // When
        repository.setActiveHours(7, 21)
        
        // Then
        verify(waterReminderDao).saveSettings(any())
    }
    
    @Test
    fun `setDailyGoal updates goal in milliliters`() = runTest {
        // Given
        val settings = WaterReminderSettingsEntity(
            dailyGoalMl = 2000
        )
        whenever(waterReminderDao.getSettings()).thenReturn(settings)
        
        // When
        repository.setDailyGoal(3000)
        
        // Then
        verify(waterReminderDao).saveSettings(any())
    }
    
    @Test
    fun `observeSettings returns flow`() {
        // Given
        val settings = WaterReminderSettingsEntity()
        whenever(waterReminderDao.observeSettings()).thenReturn(flowOf(settings))
        
        // When
        val flow = repository.observeSettings()
        
        // Then
        assertNotNull(flow)
    }
}
