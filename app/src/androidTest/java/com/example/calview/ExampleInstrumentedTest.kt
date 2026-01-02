package com.example.calview

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

/**
 * Instrumented tests for CalView app.
 * These tests run on an Android device or emulator.
 */
@RunWith(AndroidJUnit4::class)
class AppInstrumentedTest {
    
    @Test
    fun useAppContext() {
        // Context of the app under test
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.calview", appContext.packageName)
    }

    @Test
    fun appContext_isNotNull() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertNotNull(appContext)
    }

    @Test
    fun appContext_hasResources() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertNotNull(appContext.resources)
    }
}