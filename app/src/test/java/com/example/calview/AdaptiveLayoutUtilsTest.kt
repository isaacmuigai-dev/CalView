package com.example.calview.core.ui.util

import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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

    // Tests for getHorizontalPadding
    @Test
    fun getHorizontalPadding_compact_returns16dp() {
        val padding = AdaptiveLayoutUtils.getHorizontalPadding(WindowWidthSizeClass.Compact)
        assertEquals(16.dp, padding)
    }

    @Test
    fun getHorizontalPadding_medium_returns24dp() {
        val padding = AdaptiveLayoutUtils.getHorizontalPadding(WindowWidthSizeClass.Medium)
        assertEquals(24.dp, padding)
    }

    @Test
    fun getHorizontalPadding_expanded_returns32dp() {
        val padding = AdaptiveLayoutUtils.getHorizontalPadding(WindowWidthSizeClass.Expanded)
        assertEquals(32.dp, padding)
    }

    // Tests for getMaxContentWidth
    @Test
    fun getMaxContentWidth_compact_returnsInfinity() {
        val width = AdaptiveLayoutUtils.getMaxContentWidth(WindowWidthSizeClass.Compact)
        assertEquals(Dp.Infinity, width)
    }

    @Test
    fun getMaxContentWidth_medium_returns600dp() {
        val width = AdaptiveLayoutUtils.getMaxContentWidth(WindowWidthSizeClass.Medium)
        assertEquals(600.dp, width)
    }

    @Test
    fun getMaxContentWidth_expanded_returns840dp() {
        val width = AdaptiveLayoutUtils.getMaxContentWidth(WindowWidthSizeClass.Expanded)
        assertEquals(840.dp, width)
    }

    // Tests for getCardSpacing
    @Test
    fun getCardSpacing_compact_returns12dp() {
        val spacing = AdaptiveLayoutUtils.getCardSpacing(WindowWidthSizeClass.Compact)
        assertEquals(12.dp, spacing)
    }

    @Test
    fun getCardSpacing_medium_returns16dp() {
        val spacing = AdaptiveLayoutUtils.getCardSpacing(WindowWidthSizeClass.Medium)
        assertEquals(16.dp, spacing)
    }

    @Test
    fun getCardSpacing_expanded_returns20dp() {
        val spacing = AdaptiveLayoutUtils.getCardSpacing(WindowWidthSizeClass.Expanded)
        assertEquals(20.dp, spacing)
    }
}
