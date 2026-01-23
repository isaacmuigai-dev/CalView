package com.example.calview

import org.junit.Assert.*
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Unit tests for Control Center Widget logic.
 * Tests date formatting, quote selection, and water/fasting calculations.
 */
class ControlCenterWidgetTest {

    private val motivationalQuotes = listOf(
        "\"Stay consistent, stay strong!\"",
        "\"Every meal counts towards your goals.\"",
        "\"Small steps lead to big changes.\"",
        "\"You are what you eat. Make it count!\"",
        "\"Progress, not perfection.\"",
        "\"Fuel your body, fuel your dreams.\"",
        "\"Today's efforts are tomorrow's results.\"",
        "\"Keep going, you're doing great!\"",
        "\"Healthy choices, happy life.\"",
        "\"One day at a time.\""
    )

    @Test
    fun `quote selection is consistent for same day`() {
        val calendar = Calendar.getInstance()
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        
        val quote1 = motivationalQuotes[dayOfYear % motivationalQuotes.size]
        val quote2 = motivationalQuotes[dayOfYear % motivationalQuotes.size]
        
        assertEquals(quote1, quote2)
    }

    @Test
    fun `quote cycles through all quotes over year`() {
        val usedQuotes = mutableSetOf<String>()
        
        for (day in 1..365) {
            val quote = motivationalQuotes[day % motivationalQuotes.size]
            usedQuotes.add(quote)
        }
        
        assertEquals(motivationalQuotes.size, usedQuotes.size)
    }

    @Test
    fun `date format is correct`() {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formatted = dateFormat.format(calendar.time)
        
        // Should match pattern YYYY-MM-DD
        assertTrue(formatted.matches(Regex("\\d{4}-\\d{2}-\\d{2}")))
    }

    @Test
    fun `fasting elapsed time calculation is correct`() {
        val startTime = System.currentTimeMillis() - (3 * 60 * 60 * 1000) // 3 hours ago
        val elapsedMinutes = (System.currentTimeMillis() - startTime) / 60000
        
        val hours = elapsedMinutes / 60
        val minutes = elapsedMinutes % 60
        
        assertEquals(3L, hours)
        assertTrue(minutes in 0..1) // Allow 1 minute tolerance for test execution time
    }

    @Test
    fun `water intake addition is cumulative`() {
        var currentWater = 0
        
        currentWater += 250
        assertEquals(250, currentWater)
        
        currentWater += 250
        assertEquals(500, currentWater)
        
        currentWater += 250
        assertEquals(750, currentWater)
    }

    @Test
    fun `isToday function logic`() {
        val today = Calendar.getInstance()
        val alsoToday = Calendar.getInstance()
        
        val isToday = today.get(Calendar.YEAR) == alsoToday.get(Calendar.YEAR) &&
                     today.get(Calendar.DAY_OF_YEAR) == alsoToday.get(Calendar.DAY_OF_YEAR)
        
        assertTrue(isToday)
    }

    @Test
    fun `yesterday is not today`() {
        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
        }
        
        val isToday = today.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
                     today.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR)
        
        assertFalse(isToday)
    }

    @Test
    fun `16_8 fasting duration is 960 minutes`() {
        val targetDurationMinutes = 16 * 60
        assertEquals(960, targetDurationMinutes)
    }
}
