package com.example.calview.feature.onboarding

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for OnboardingViewModel and language support.
 * These tests verify the public APIs and data structures used in onboarding.
 */
class OnboardingViewModelTest {

    @Test
    fun `supported languages list is not empty`() {
        assertTrue("supportedLanguages should not be empty", supportedLanguages.isNotEmpty())
    }
    
    @Test
    fun `supported languages contains English as first option`() {
        val firstLanguage = supportedLanguages.first()
        assertEquals("First language should be English", "English", firstLanguage.name)
        assertEquals("First language code should be EN", "EN", firstLanguage.code)
        assertEquals("First language flag should be US flag", "🇺🇸", firstLanguage.flag)
    }
    
    @Test
    fun `supported languages contains at least 10 languages`() {
        // The app supports multiple languages for accessibility
        assertTrue("Should have at least 10 languages", supportedLanguages.size >= 10)
    }
    
    @Test
    fun `all languages have valid properties`() {
        supportedLanguages.forEach { language ->
            assertTrue("Language code should not be blank: ${language.name}", language.code.isNotBlank())
            assertTrue("Language name should not be blank: ${language.code}", language.name.isNotBlank())
            assertTrue("Language flag should not be blank: ${language.code}", language.flag.isNotBlank())
        }
    }
    
    @Test
    fun `all languages have unique codes`() {
        val codes = supportedLanguages.map { it.code }
        val uniqueCodes = codes.toSet()
        assertEquals("All language codes should be unique", codes.size, uniqueCodes.size)
    }
    
    @Test
    fun `supportedLanguages contains Swahili for Kenyan users`() {
        val swahili = supportedLanguages.find { it.code == "SW" }
        assertNotNull("Swahili should be in supported languages", swahili)
        assertEquals("Kiswahili", swahili?.name)
    }
}
