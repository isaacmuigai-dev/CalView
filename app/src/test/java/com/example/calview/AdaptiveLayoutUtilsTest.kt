package com.example.calview.core.ui.util

import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for AdaptiveLayoutUtils helper functions.
 */
class AdaptiveLayoutUtilsTest {

    @Test
    fun getGridColumns_compact_returns1() {
        val columns = AdaptiveLayoutUtils.getGridColumns(WindowWidthSizeClass.Compact)
        assertEquals(1, columns)
    }

    @Test
    fun getGridColumns_medium_returns2() {
        val columns = AdaptiveLayoutUtils.getGridColumns(WindowWidthSizeClass.Medium)
        assertEquals(2, columns)
    }

    @Test
    fun getGridColumns_expanded_returns3() {
        val columns = AdaptiveLayoutUtils.getGridColumns(WindowWidthSizeClass.Expanded)
        assertEquals(3, columns)
    }

    @Test
    fun getStatCardColumns_compact_returns2() {
        val columns = AdaptiveLayoutUtils.getStatCardColumns(WindowWidthSizeClass.Compact)
        assertEquals(2, columns)
    }

    @Test
    fun getStatCardColumns_medium_returns3() {
        val columns = AdaptiveLayoutUtils.getStatCardColumns(WindowWidthSizeClass.Medium)
        assertEquals(3, columns)
    }

    @Test
    fun getStatCardColumns_expanded_returns4() {
        val columns = AdaptiveLayoutUtils.getStatCardColumns(WindowWidthSizeClass.Expanded)
        assertEquals(4, columns)
    }

    @Test
    fun shouldUseNavigationRail_compact_returnsFalse() {
        val useRail = AdaptiveLayoutUtils.shouldUseNavigationRail(WindowWidthSizeClass.Compact)
        assertFalse(useRail)
    }

    @Test
    fun shouldUseNavigationRail_medium_returnsTrue() {
        val useRail = AdaptiveLayoutUtils.shouldUseNavigationRail(WindowWidthSizeClass.Medium)
        assertTrue(useRail)
    }

    @Test
    fun shouldUseNavigationRail_expanded_returnsTrue() {
        val useRail = AdaptiveLayoutUtils.shouldUseNavigationRail(WindowWidthSizeClass.Expanded)
        assertTrue(useRail)
    }

    @Test
    fun isCompactHeight_compact_returnsTrue() {
        val isCompact = AdaptiveLayoutUtils.isCompactHeight(WindowHeightSizeClass.Compact)
        assertTrue(isCompact)
    }

    @Test
    fun isCompactHeight_medium_returnsFalse() {
        val isCompact = AdaptiveLayoutUtils.isCompactHeight(WindowHeightSizeClass.Medium)
        assertFalse(isCompact)
    }

    @Test
    fun isCompactHeight_expanded_returnsFalse() {
        val isCompact = AdaptiveLayoutUtils.isCompactHeight(WindowHeightSizeClass.Expanded)
        assertFalse(isCompact)
    }
}
