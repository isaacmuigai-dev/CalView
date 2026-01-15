package com.example.calview

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.Month
import java.time.Period

/**
 * Unit tests for DOB to age calculation logic
 */
class DobAgeCalculationTest {

    @Test
    fun `age calculation for 25 year old`() {
        val birthYear = LocalDate.now().year - 25
        val birthDate = LocalDate.of(birthYear, Month.JANUARY, 15)
        val today = LocalDate.now()
        
        val age = Period.between(birthDate, today).years
        
        // Age should be approximately 25 (depending on current month)
        assertTrue(age in 24..25)
    }
    
    @Test
    fun `age calculation handles leap year birthday`() {
        val birthDate = LocalDate.of(2000, Month.FEBRUARY, 29)
        val today = LocalDate.of(2025, Month.MARCH, 1)
        
        val age = Period.between(birthDate, today).years
        
        assertEquals(25, age)
    }
    
    @Test
    fun `age is 0 for newborn`() {
        val today = LocalDate.now()
        val birthDate = today.minusMonths(6)
        
        val age = Period.between(birthDate, today).years
        
        assertEquals(0, age)
    }
    
    @Test
    fun `age calculation for future date returns negative`() {
        val futureDate = LocalDate.now().plusYears(5)
        val today = LocalDate.now()
        
        val age = Period.between(futureDate, today).years
        
        assertTrue(age < 0)
    }
    
    @Test
    fun `month parsing handles uppercase`() {
        val monthString = "JANUARY"
        val month = try {
            Month.valueOf(monthString.uppercase())
        } catch (e: Exception) {
            Month.JANUARY
        }
        
        assertEquals(Month.JANUARY, month)
    }
    
    @Test
    fun `month parsing handles mixed case`() {
        val monthString = "February"
        val month = try {
            Month.valueOf(monthString.uppercase())
        } catch (e: Exception) {
            Month.JANUARY
        }
        
        assertEquals(Month.FEBRUARY, month)
    }
    
    @Test
    fun `day coercion handles invalid day 31 for February`() {
        val day = 31
        val coercedDay = day.coerceIn(1, 28)
        
        assertEquals(28, coercedDay)
    }
    
    @Test
    fun `day coercion handles negative day`() {
        val day = -5
        val coercedDay = day.coerceIn(1, 28)
        
        assertEquals(1, coercedDay)
    }
}
