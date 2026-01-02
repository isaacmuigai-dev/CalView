package com.example.calview

import com.example.calview.feature.trends.ProgressUiState
import com.example.calview.feature.trends.DailyCalories
import com.example.calview.feature.trends.WeightEntry
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

/**
 * Unit tests for ProgressUiState data class and its default values.
 */
class ProgressUiStateTest {

    @Test
    fun defaultState_hasCorrectDefaultValues() {
        val state = ProgressUiState()
        
        assertEquals(0f, state.currentWeight, 0.01f)
        assertEquals(0f, state.goalWeight, 0.01f)
        assertEquals(0, state.height)
        assertEquals(0f, state.bmi, 0.01f)
        assertEquals("Healthy", state.bmiCategory)
        assertEquals(0f, state.weightProgress, 0.01f)
        assertTrue(state.isLoading)
    }

    @Test
    fun defaultState_hasDefaultGoals() {
        val state = ProgressUiState()
        
        assertEquals(2000, state.calorieGoal)
        assertEquals(120, state.proteinGoal)
        assertEquals(200, state.carbsGoal)
        assertEquals(65, state.fatsGoal)
        assertEquals(10000, state.stepsGoal)
    }

    @Test
    fun defaultState_hasTodayProgressZeros() {
        val state = ProgressUiState()
        
        assertEquals(0, state.todayCalories)
        assertEquals(0f, state.todayProtein, 0.01f)
        assertEquals(0f, state.todayCarbs, 0.01f)
        assertEquals(0f, state.todayFats, 0.01f)
        assertEquals(0, state.todaySteps)
        assertEquals(0, state.caloriesBurned)
    }

    @Test
    fun defaultState_hasEmptyLists() {
        val state = ProgressUiState()
        
        assertTrue(state.weeklyCalories.isEmpty())
        assertTrue(state.weightHistory.isEmpty())
        assertEquals(7, state.completedDays.size)
    }

    @Test
    fun defaultState_hasStreakDefaults() {
        val state = ProgressUiState()
        
        assertEquals(0, state.dayStreak)
        assertEquals(0, state.bestStreak)
        assertEquals(0, state.weeklyAverageCalories)
    }

    @Test
    fun dailyCalories_createsCorrectly() {
        val daily = DailyCalories(
            day = "Mon",
            calories = 1800,
            protein = 100f,
            carbs = 200f,
            fats = 50f
        )
        
        assertEquals("Mon", daily.day)
        assertEquals(1800, daily.calories)
        assertEquals(100f, daily.protein, 0.01f)
    }

    @Test
    fun weightEntry_createsCorrectly() {
        val today = LocalDate.now()
        val entry = WeightEntry(
            date = today,
            weight = 75.5f
        )
        
        assertEquals(today, entry.date)
        assertEquals(75.5f, entry.weight, 0.01f)
    }

    @Test
    fun state_copyUpdatesValues() {
        val state = ProgressUiState()
        val updated = state.copy(
            currentWeight = 80f,
            bmi = 25.5f,
            bmiCategory = "Overweight",
            isLoading = false
        )
        
        assertEquals(80f, updated.currentWeight, 0.01f)
        assertEquals(25.5f, updated.bmi, 0.01f)
        assertEquals("Overweight", updated.bmiCategory)
        assertFalse(updated.isLoading)
        // Original should be unchanged
        assertEquals(0f, state.currentWeight, 0.01f)
        assertTrue(state.isLoading)
    }
}
