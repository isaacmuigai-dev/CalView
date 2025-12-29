package com.example.calview.feature.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calview.core.ui.theme.Inter
import com.example.calview.feature.onboarding.components.OnboardingScreenLayout
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Height and Weight input screen with Imperial/Metric toggle and scroll pickers.
 */
@Composable
fun HeightWeightScreen(
    currentStep: Int = 5,
    totalSteps: Int = 7,
    isMetric: Boolean = false,
    heightFeet: Int = 5,
    heightInches: Int = 6,
    heightCm: Int = 170,
    weightLb: Int = 150,
    weightKg: Int = 68,
    onMetricToggle: (Boolean) -> Unit,
    onHeightFeetChanged: (Int) -> Unit,
    onHeightInchesChanged: (Int) -> Unit,
    onHeightCmChanged: (Int) -> Unit,
    onWeightLbChanged: (Int) -> Unit,
    onWeightKgChanged: (Int) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    OnboardingScreenLayout(
        currentStep = currentStep,
        totalSteps = totalSteps,
        title = "Height & Weight",
        subtitle = "This will be taken into account when calculating your daily nutrition goals.",
        onBack = onBack,
        onContinue = onContinue,
        continueEnabled = true
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        // Imperial/Metric Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Imperial",
                fontFamily = Inter,
                fontWeight = if (!isMetric) FontWeight.Bold else FontWeight.Normal,
                fontSize = 16.sp,
                color = if (!isMetric) Color.Black else Color.Gray
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Switch(
                checked = isMetric,
                onCheckedChange = onMetricToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF1C1C1E),
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFFE5E5E5)
                )
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = "Metric",
                fontFamily = Inter,
                fontWeight = if (isMetric) FontWeight.Bold else FontWeight.Normal,
                fontSize = 16.sp,
                color = if (isMetric) Color.Black else Color.Gray
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Column headers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                text = "Height",
                fontFamily = Inter,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "Weight",
                fontFamily = Inter,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier.weight(0.5f),
                textAlign = TextAlign.Center
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Picker area
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            if (isMetric) {
                // Metric: cm picker
                WheelPicker(
                    items = (100..220).map { "$it cm" },
                    selectedIndex = heightCm - 100,
                    onSelectedIndexChange = { onHeightCmChanged(it + 100) },
                    modifier = Modifier.weight(1f)
                )
            } else {
                // Imperial: feet and inches pickers
                WheelPicker(
                    items = (3..8).map { "$it ft" },
                    selectedIndex = heightFeet - 3,
                    onSelectedIndexChange = { onHeightFeetChanged(it + 3) },
                    modifier = Modifier.weight(0.5f)
                )
                
                WheelPicker(
                    items = (0..11).map { "$it in" },
                    selectedIndex = heightInches,
                    onSelectedIndexChange = { onHeightInchesChanged(it) },
                    modifier = Modifier.weight(0.5f)
                )
            }
            
            // Weight picker
            if (isMetric) {
                WheelPicker(
                    items = (30..200).map { "$it kg" },
                    selectedIndex = weightKg - 30,
                    onSelectedIndexChange = { onWeightKgChanged(it + 30) },
                    modifier = Modifier.weight(0.5f)
                )
            } else {
                WheelPicker(
                    items = (66..440).map { "$it lb" },
                    selectedIndex = weightLb - 66,
                    onSelectedIndexChange = { onWeightLbChanged(it + 66) },
                    modifier = Modifier.weight(0.5f)
                )
            }
        }
    }
}

/**
 * Wheel picker component with scroll snapping.
 */
@Composable
fun WheelPicker(
    items: List<String>,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val itemHeight = 44.dp
    val visibleItems = 5
    
    // Initial scroll to selected index
    LaunchedEffect(selectedIndex) {
        listState.scrollToItem(selectedIndex)
    }
    
    // Snap to nearest item when scrolling stops
    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val firstVisibleIndex = listState.firstVisibleItemIndex
            val offset = listState.firstVisibleItemScrollOffset
            val targetIndex = if (offset > itemHeight.value / 2) {
                firstVisibleIndex + 1
            } else {
                firstVisibleIndex
            }.coerceIn(0, items.lastIndex)
            
            if (targetIndex != selectedIndex) {
                onSelectedIndexChange(targetIndex)
            }
            scope.launch {
                listState.animateScrollToItem(targetIndex)
            }
        }
    }
    
    Box(
        modifier = modifier.height(itemHeight * visibleItems),
        contentAlignment = Alignment.Center
    ) {
        // Selection highlight
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight)
                .padding(horizontal = 8.dp),
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFF5F5F5),
            border = BorderStroke(1.dp, Color(0xFFE5E5E5))
        ) {}
        
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = itemHeight * 2),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(items.size) { index ->
                val distanceFromCenter = abs(index - selectedIndex)
                val alpha = when (distanceFromCenter) {
                    0 -> 1f
                    1 -> 0.5f
                    2 -> 0.3f
                    else -> 0.15f
                }
                
                Box(
                    modifier = Modifier
                        .height(itemHeight)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = items[index],
                        fontFamily = Inter,
                        fontWeight = if (index == selectedIndex) FontWeight.Bold else FontWeight.Normal,
                        fontSize = if (index == selectedIndex) 18.sp else 16.sp,
                        color = Color.Black.copy(alpha = alpha),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
