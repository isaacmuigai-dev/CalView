package com.example.calview.core.ai.voice

import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.type.GenerateContentResponse
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

/**
 * Unit tests for VoiceFoodParser
 */
class VoiceFoodParserTest {
    
    @Mock
    private lateinit var generativeModel: GenerativeModel
    
    @Mock
    private lateinit var generateContentResponse: GenerateContentResponse
    
    private lateinit var parser: VoiceFoodParser
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        parser = VoiceFoodParser(generativeModel)
    }
    
    @Test
    fun `parseVoiceInput returns success for valid JSON response`() = runTest {
        // Given
        val jsonResponse = """
            [
                {"name": "Eggs", "quantity": 2, "unit": "piece", "calories": 156, "protein": 12, "carbs": 1, "fat": 11},
                {"name": "Toast", "quantity": 1, "unit": "slice", "calories": 79, "protein": 3, "carbs": 15, "fat": 1}
            ]
        """.trimIndent()
        
        whenever(generateContentResponse.text).thenReturn(jsonResponse)
        whenever(generativeModel.generateContent(any<String>())).thenReturn(generateContentResponse)
        
        // When
        val result = parser.parseVoiceInput("I had 2 eggs and a slice of toast")
        
        // Then
        assertTrue(result.isSuccess)
        val foods = result.getOrNull()
        assertNotNull(foods)
        assertEquals(2, foods!!.size)
        assertEquals("Eggs", foods[0].name)
        assertEquals(2f, foods[0].quantity)
        assertEquals(156, foods[0].calories)
    }
    
    @Test
    fun `parseVoiceInput handles markdown code block response`() = runTest {
        // Given
        val jsonResponse = """
            ```json
            [
                {"name": "Coffee", "quantity": 1, "unit": "cup", "calories": 5, "protein": 0, "carbs": 0, "fat": 0}
            ]
            ```
        """.trimIndent()
        
        whenever(generateContentResponse.text).thenReturn(jsonResponse)
        whenever(generativeModel.generateContent(any<String>())).thenReturn(generateContentResponse)
        
        // When
        val result = parser.parseVoiceInput("I had a coffee")
        
        // Then
        assertTrue(result.isSuccess)
        val foods = result.getOrNull()
        assertNotNull(foods)
        assertEquals(1, foods!!.size)
        assertEquals("Coffee", foods[0].name)
    }
    
    @Test
    fun `parseVoiceInput returns empty list for invalid JSON`() = runTest {
        // Given
        val invalidJson = "Not a valid JSON response"
        
        whenever(generateContentResponse.text).thenReturn(invalidJson)
        whenever(generativeModel.generateContent(any<String>())).thenReturn(generateContentResponse)
        
        // When
        val result = parser.parseVoiceInput("random text")
        
        // Then
        assertTrue(result.isSuccess)
        val foods = result.getOrNull()
        assertNotNull(foods)
        assertTrue(foods!!.isEmpty())
    }
    
    @Test
    fun `parseVoiceInput returns failure for exception`() = runTest {
        // Given
        whenever(generativeModel.generateContent(any<String>()))
            .thenThrow(RuntimeException("API Error"))
        
        // When
        val result = parser.parseVoiceInput("test input")
        
        // Then
        assertTrue(result.isFailure)
        assertEquals("API Error", result.exceptionOrNull()?.message)
    }
    
    @Test
    fun `parseVoiceInput returns failure for null response`() = runTest {
        // Given
        whenever(generateContentResponse.text).thenReturn(null)
        whenever(generativeModel.generateContent(any<String>())).thenReturn(generateContentResponse)
        
        // When
        val result = parser.parseVoiceInput("test input")
        
        // Then
        assertTrue(result.isFailure)
    }
    
    @Test
    fun `ParsedFoodItem has correct default values`() {
        // When
        val item = ParsedFoodItem(
            name = "Test Food",
            quantity = 1f,
            unit = "serving",
            calories = 100,
            protein = 10,
            carbs = 20,
            fat = 5
        )
        
        // Then
        assertEquals("Test Food", item.name)
        assertEquals(1f, item.quantity)
        assertEquals("serving", item.unit)
        assertEquals(100, item.calories)
        assertEquals(10, item.protein)
        assertEquals(20, item.carbs)
        assertEquals(5, item.fat)
    }
    
    @Test
    fun `parseVoiceInput handles partial JSON with missing fields`() = runTest {
        // Given - JSON with missing optional fields
        val jsonResponse = """
            [
                {"name": "Apple", "calories": 95}
            ]
        """.trimIndent()
        
        whenever(generateContentResponse.text).thenReturn(jsonResponse)
        whenever(generativeModel.generateContent(any<String>())).thenReturn(generateContentResponse)
        
        // When
        val result = parser.parseVoiceInput("I ate an apple")
        
        // Then
        assertTrue(result.isSuccess)
        val foods = result.getOrNull()
        assertNotNull(foods)
        assertEquals(1, foods!!.size)
        assertEquals("Apple", foods[0].name)
        assertEquals(95, foods[0].calories)
        assertEquals(1f, foods[0].quantity) // Default value
        assertEquals("serving", foods[0].unit) // Default value
    }
}
