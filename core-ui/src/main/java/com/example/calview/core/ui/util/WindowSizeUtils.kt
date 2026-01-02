package com.example.calview.core.ui.util

import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * CompositionLocal for accessing WindowSizeClass throughout the app.
 * This allows any composable to adapt its layout based on screen size.
 */
val LocalWindowSizeClass = staticCompositionLocalOf<WindowSizeClass> {
    error("WindowSizeClass not provided. Make sure to provide it in MainActivity.")
}

/**
 * Helper object for adaptive layout decisions based on WindowSizeClass.
 */
object AdaptiveLayoutUtils {
    
    /**
     * Returns the number of columns to use in a grid layout based on width class.
     */
    fun getGridColumns(widthSizeClass: WindowWidthSizeClass): Int {
        return when (widthSizeClass) {
            WindowWidthSizeClass.Compact -> 1
            WindowWidthSizeClass.Medium -> 2
            WindowWidthSizeClass.Expanded -> 3
            else -> 1
        }
    }
    
    /**
     * Returns the number of stat card columns based on width class.
     */
    fun getStatCardColumns(widthSizeClass: WindowWidthSizeClass): Int {
        return when (widthSizeClass) {
            WindowWidthSizeClass.Compact -> 2
            WindowWidthSizeClass.Medium -> 3
            WindowWidthSizeClass.Expanded -> 4
            else -> 2
        }
    }
    
    /**
     * Returns horizontal padding based on width class.
     */
    fun getHorizontalPadding(widthSizeClass: WindowWidthSizeClass): Dp {
        return when (widthSizeClass) {
            WindowWidthSizeClass.Compact -> 16.dp
            WindowWidthSizeClass.Medium -> 24.dp
            WindowWidthSizeClass.Expanded -> 32.dp
            else -> 16.dp
        }
    }
    
    /**
     * Returns whether to use a navigation rail instead of bottom navigation.
     */
    fun shouldUseNavigationRail(widthSizeClass: WindowWidthSizeClass): Boolean {
        return widthSizeClass != WindowWidthSizeClass.Compact
    }
    
    /**
     * Returns the max content width for centered layouts on wider screens.
     */
    fun getMaxContentWidth(widthSizeClass: WindowWidthSizeClass): Dp {
        return when (widthSizeClass) {
            WindowWidthSizeClass.Compact -> Dp.Infinity
            WindowWidthSizeClass.Medium -> 600.dp
            WindowWidthSizeClass.Expanded -> 840.dp
            else -> Dp.Infinity
        }
    }
    
    /**
     * Returns whether the screen is in a compact height mode (e.g., landscape phone).
     */
    fun isCompactHeight(heightSizeClass: WindowHeightSizeClass): Boolean {
        return heightSizeClass == WindowHeightSizeClass.Compact
    }
    
    /**
     * Returns card spacing based on width class.
     */
    fun getCardSpacing(widthSizeClass: WindowWidthSizeClass): Dp {
        return when (widthSizeClass) {
            WindowWidthSizeClass.Compact -> 12.dp
            WindowWidthSizeClass.Medium -> 16.dp
            WindowWidthSizeClass.Expanded -> 20.dp
            else -> 12.dp
        }
    }
}
