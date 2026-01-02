package com.example.calview

import org.junit.Test
import org.junit.Assert.*

/**
 * Sanity tests to verify basic app functionality.
 */
class CalViewSanityTest {

    @Test
    fun dashboardState_defaultIsNotNull() {
        val state = com.example.calview.feature.dashboard.DashboardState()
        assertNotNull(state)
    }

    @Test
    fun progressUiState_defaultIsNotNull() {
        val state = com.example.calview.feature.trends.ProgressUiState()
        assertNotNull(state)
    }

    @Test
    fun dashboardState_mealsListIsInitialized() {
        val state = com.example.calview.feature.dashboard.DashboardState()
        assertNotNull(state.meals)
        assertEquals(0, state.meals.size)
    }

    @Test
    fun progressUiState_weeklyCaloriesIsInitialized() {
        val state = com.example.calview.feature.trends.ProgressUiState()
        assertNotNull(state.weeklyCalories)
        assertEquals(0, state.weeklyCalories.size)
    }
}