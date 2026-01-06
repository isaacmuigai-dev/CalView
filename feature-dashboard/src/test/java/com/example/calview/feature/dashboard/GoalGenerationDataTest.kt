package com.example.calview.feature.dashboard

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for GoalGenerationData data class used in AutoGenerateGoalsScreen.
 */
class GoalGenerationDataTest {

    @Test
    fun `default creation with all required fields`() {
        val data = GoalGenerationData(
            workoutsPerWeek = "3-5",
            heightCm = 170,
            weightKg = 70f,
            goal = "Lose Weight",
            desiredWeightKg = 65f,
            weightChangePerWeek = 0.5f,
            gender = "Male",
            age = 25
        )
        
        assertEquals("3-5", data.workoutsPerWeek)
        assertEquals(170, data.heightCm)
        assertEquals(70f, data.weightKg)
        assertEquals("Lose Weight", data.goal)
        assertEquals(65f, data.desiredWeightKg)
        assertEquals(0.5f, data.weightChangePerWeek)
        assertEquals("Male", data.gender)
        assertEquals(25, data.age)
    }

    @Test
    fun `maintain goal has same desired weight as current`() {
        val data = GoalGenerationData(
            workoutsPerWeek = "0-2",
            heightCm = 175,
            weightKg = 80f,
            goal = "Maintain",
            desiredWeightKg = 80f,     // Same as current weight
            weightChangePerWeek = 0f,   // No change for maintain
            gender = "Female",
            age = 30
        )
        
        assertEquals("Maintain", data.goal)
        assertEquals(data.weightKg, data.desiredWeightKg)
        assertEquals(0f, data.weightChangePerWeek)
    }

    @Test
    fun `gain weight goal has higher desired weight`() {
        val data = GoalGenerationData(
            workoutsPerWeek = "6+",
            heightCm = 180,
            weightKg = 70f,
            goal = "Gain Weight",
            desiredWeightKg = 75f,      // Higher than current
            weightChangePerWeek = 0.3f,
            gender = "Male",
            age = 22
        )
        
        assertEquals("Gain Weight", data.goal)
        assertTrue(data.desiredWeightKg > data.weightKg)
    }

    @Test
    fun `lose weight goal has lower desired weight`() {
        val data = GoalGenerationData(
            workoutsPerWeek = "3-5",
            heightCm = 165,
            weightKg = 85f,
            goal = "Lose Weight",
            desiredWeightKg = 75f,      // Lower than current
            weightChangePerWeek = 0.5f,
            gender = "Female",
            age = 35
        )
        
        assertEquals("Lose Weight", data.goal)
        assertTrue(data.desiredWeightKg < data.weightKg)
    }

    @Test
    fun `activity levels are valid options`() {
        val validLevels = listOf("0-2", "3-5", "6+")
        
        validLevels.forEach { level ->
            val data = GoalGenerationData(
                workoutsPerWeek = level,
                heightCm = 170,
                weightKg = 70f,
                goal = "Maintain",
                desiredWeightKg = 70f,
                weightChangePerWeek = 0f,
                gender = "Male",
                age = 25
            )
            assertTrue("$level should be a valid activity level", validLevels.contains(data.workoutsPerWeek))
        }
    }

    @Test
    fun `valid goals are accepted`() {
        val validGoals = listOf("Lose Weight", "Maintain", "Gain Weight")
        
        validGoals.forEach { goal ->
            val data = GoalGenerationData(
                workoutsPerWeek = "3-5",
                heightCm = 170,
                weightKg = 70f,
                goal = goal,
                desiredWeightKg = 70f,
                weightChangePerWeek = 0f,
                gender = "Male",
                age = 25
            )
            assertTrue("$goal should be a valid goal", validGoals.contains(data.goal))
        }
    }

    @Test
    fun `pace is within expected range`() {
        val data = GoalGenerationData(
            workoutsPerWeek = "3-5",
            heightCm = 170,
            weightKg = 75f,
            goal = "Lose Weight",
            desiredWeightKg = 65f,
            weightChangePerWeek = 0.8f,
            gender = "Female",
            age = 28
        )
        
        // Pace should be between 0.2 and 1.0 kg/week
        assertTrue(data.weightChangePerWeek >= 0.2f || data.weightChangePerWeek == 0f)
        assertTrue(data.weightChangePerWeek <= 1.0f)
    }
}
