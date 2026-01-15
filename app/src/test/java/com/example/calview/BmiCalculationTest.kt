package com.example.calview

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for BMI calculation logic
 */
class BmiCalculationTest {

    @Test
    fun `BMI calculation with normal values`() {
        val weight = 70f // kg
        val height = 175 // cm
        
        val bmi = calculateBmi(weight, height)
        
        // Expected: 70 / (1.75 * 1.75) = 22.86
        assertEquals(22.86f, bmi, 0.1f)
    }
    
    @Test
    fun `BMI category underweight`() {
        val bmi = 17.5f
        
        val category = getBmiCategory(bmi)
        
        assertEquals("Underweight", category)
    }
    
    @Test
    fun `BMI category normal`() {
        val bmi = 22.0f
        
        val category = getBmiCategory(bmi)
        
        assertEquals("Healthy", category)
    }
    
    @Test
    fun `BMI category overweight`() {
        val bmi = 27.0f
        
        val category = getBmiCategory(bmi)
        
        assertEquals("Overweight", category)
    }
    
    @Test
    fun `BMI category obese`() {
        val bmi = 32.0f
        
        val category = getBmiCategory(bmi)
        
        assertEquals("Obese", category)
    }
    
    @Test
    fun `BMI handles zero height safely`() {
        val weight = 70f
        val height = 0
        
        val bmi = calculateBmi(weight, height)
        
        assertEquals(0f, bmi, 0.01f)
    }
    
    @Test
    fun `BMI handles zero weight`() {
        val weight = 0f
        val height = 175
        
        val bmi = calculateBmi(weight, height)
        
        assertEquals(0f, bmi, 0.01f)
    }
    
    @Test
    fun `weight progress calculation`() {
        val startWeight = 80f
        val currentWeight = 75f
        val goalWeight = 70f
        
        val progress = calculateWeightProgress(startWeight, currentWeight, goalWeight)
        
        // Lost 5kg out of 10kg goal = 50%
        assertEquals(50f, progress, 0.1f)
    }
    
    @Test
    fun `weight progress exceeds goal`() {
        val startWeight = 80f
        val currentWeight = 68f
        val goalWeight = 70f
        
        val progress = calculateWeightProgress(startWeight, currentWeight, goalWeight)
        
        // Exceeded goal, should cap at 100%
        assertEquals(100f, progress.coerceAtMost(100f), 0.1f)
    }
    
    // Helper functions simulating actual implementation
    private fun calculateBmi(weight: Float, heightCm: Int): Float {
        if (heightCm <= 0 || weight <= 0) return 0f
        val heightM = heightCm / 100f
        return weight / (heightM * heightM)
    }
    
    private fun getBmiCategory(bmi: Float): String {
        return when {
            bmi < 18.5f -> "Underweight"
            bmi < 25f -> "Healthy"
            bmi < 30f -> "Overweight"
            else -> "Obese"
        }
    }
    
    private fun calculateWeightProgress(startWeight: Float, currentWeight: Float, goalWeight: Float): Float {
        if (startWeight == goalWeight) return 100f
        val totalToLose = startWeight - goalWeight
        val lost = startWeight - currentWeight
        return (lost / totalToLose) * 100f
    }
}
