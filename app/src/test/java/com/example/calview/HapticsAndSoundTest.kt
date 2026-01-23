package com.example.calview

import com.example.calview.core.ui.util.SoundManager
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for HapticsManager and SoundManager.
 * Note: Actual vibration/audio testing requires instrumented tests on a device.
 * These tests verify the API contracts and enum values.
 */
class HapticsAndSoundTest {

    @Test
    fun `SoundType enum has all expected values`() {
        val expectedTypes = listOf(
            SoundManager.SoundType.SUCCESS,
            SoundManager.SoundType.ERROR,
            SoundManager.SoundType.CLICK,
            SoundManager.SoundType.COMPLETE
        )
        
        assertEquals(4, SoundManager.SoundType.values().size)
        expectedTypes.forEach { type ->
            assertTrue("Missing SoundType: $type", SoundManager.SoundType.values().contains(type))
        }
    }

    @Test
    fun `SoundType SUCCESS is first in enum`() {
        assertEquals(0, SoundManager.SoundType.SUCCESS.ordinal)
    }

    @Test
    fun `SoundType COMPLETE is last in enum`() {
        assertEquals(3, SoundManager.SoundType.COMPLETE.ordinal)
    }
}
