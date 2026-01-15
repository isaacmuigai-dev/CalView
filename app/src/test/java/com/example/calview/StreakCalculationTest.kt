package com.example.calview

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for streak calculation logic
 */
class StreakCalculationTest {

    @Test
    fun `streak of 0 when no meals logged`() {
        val mealsPerDay = listOf<Int>() // Empty list
        
        val streak = calculateStreak(mealsPerDay)
        
        assertEquals(0, streak)
    }
    
    @Test
    fun `streak of 1 when only today has meals`() {
        val mealsPerDay = listOf(1) // Only today
        
        val streak = calculateStreak(mealsPerDay)
        
        assertEquals(1, streak)
    }
    
    @Test
    fun `streak counts consecutive days`() {
        // 3 consecutive days with meals (today, yesterday, day before)
        val mealsPerDay = listOf(2, 1, 3)
        
        val streak = calculateStreak(mealsPerDay)
        
        assertEquals(3, streak)
    }
    
    @Test
    fun `streak breaks on day with no meals`() {
        // Today=2, yesterday=1, 2 days ago=0 (break), 3 days ago=1
        val mealsPerDay = listOf(2, 1, 0, 1)
        
        val streak = calculateStreak(mealsPerDay)
        
        assertEquals(2, streak) // Only counts until the break
    }
    
    @Test
    fun `best streak calculated correctly`() {
        val dailyMeals = listOf(
            listOf(1, 1, 0, 1, 1, 1, 0) // Two streaks: 2 and 3
        )
        
        val bestStreak = calculateBestStreak(dailyMeals.flatten())
        
        assertEquals(3, bestStreak)
    }
    
    @Test
    fun `streak day grammar singular`() {
        val streak = 1
        val text = if (streak == 1) "$streak day" else "$streak days"
        
        assertEquals("1 day", text)
    }
    
    @Test
    fun `streak day grammar plural`() {
        val streak = 5
        val text = if (streak == 1) "$streak day" else "$streak days"
        
        assertEquals("5 days", text)
    }
    
    @Test
    fun `streak day grammar zero`() {
        val streak = 0
        val text = if (streak == 1) "$streak day" else "$streak days"
        
        assertEquals("0 days", text)
    }
    
    // Helper function simulating streak calculation
    private fun calculateStreak(mealsPerDay: List<Int>): Int {
        var streak = 0
        for (meals in mealsPerDay) {
            if (meals > 0) {
                streak++
            } else {
                break
            }
        }
        return streak
    }
    
    private fun calculateBestStreak(mealsPerDay: List<Int>): Int {
        var bestStreak = 0
        var currentStreak = 0
        
        for (meals in mealsPerDay) {
            if (meals > 0) {
                currentStreak++
                bestStreak = maxOf(bestStreak, currentStreak)
            } else {
                currentStreak = 0
            }
        }
        return bestStreak
    }
}
