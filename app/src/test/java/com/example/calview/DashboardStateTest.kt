package com.example.calview

import com.example.calview.feature.dashboard.DashboardState
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

/**
 * Unit tests for DashboardState data class and its default values.
 */
class DashboardStateTest {

    @Test
    fun defaultState_hasCorrectDefaultValues() {
        val state = DashboardState()
        
        assertEquals(0, state.consumedCalories)
        assertEquals(0, state.remainingCalories)
        assertEquals(2000, state.goalCalories)
        assertEquals(0, state.waterConsumed)
        assertEquals(0, state.steps.toInt())
        assertEquals(0, state.caloriesBurned)
        assertFalse(state.isHealthConnected)
        assertFalse(state.rolloverCaloriesEnabled)
        assertFalse(state.addCaloriesBackEnabled)
        assertEquals(0, state.currentStreak)
    }

    @Test
    fun defaultState_hasMacroGoals() {
        val state = DashboardState()
        
        // Default macro goals for 2000 calories
        assertEquals(125, state.proteinGoal)  // 25% of 2000 cal / 4 cal per gram
        assertEquals(250, state.carbsGoal)    // 50% of 2000 cal / 4 cal per gram
        assertEquals(56, state.fatsGoal)      // 25% of 2000 cal / 9 cal per gram
    }

    @Test
    fun defaultState_hasEmptyMealsList() {
        val state = DashboardState()
        
        assertTrue(state.meals.isEmpty())
        assertTrue(state.recentUploads.isEmpty())
    }

    @Test
    fun defaultState_has7CompletedDays() {
        val state = DashboardState()
        
        assertEquals(7, state.completedDays.size)
        assertTrue(state.completedDays.all { !it })
    }

    @Test
    fun state_copyUpdatesValues() {
        val state = DashboardState()
        val updated = state.copy(
            consumedCalories = 1500,
            remainingCalories = 500,
            waterConsumed = 64
        )
        
        assertEquals(1500, updated.consumedCalories)
        assertEquals(500, updated.remainingCalories)
        assertEquals(64, updated.waterConsumed)
        // Original should be unchanged
        assertEquals(0, state.consumedCalories)
    }

    @Test
    fun stepsGoal_hasDefaultOf10000() {
        val state = DashboardState()
        assertEquals(10000, state.stepsGoal)
    }
}
