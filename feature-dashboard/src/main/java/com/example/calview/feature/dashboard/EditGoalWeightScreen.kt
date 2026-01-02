package com.example.calview.feature.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calview.core.ui.theme.Inter
import kotlinx.coroutines.flow.collectLatest

/**
 * Edit Goal Weight screen with horizontal ruler scale picker.
 * Shows "Maintain Weight", "Lose Weight", or "Gain Weight" based on comparison with current weight.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditGoalWeightScreen(
    currentWeight: Float = 119f,
    goalWeight: Float = 119f,
    onBack: () -> Unit,
    onSave: (Float) -> Unit
) {
    var selectedWeight by remember { mutableFloatStateOf(goalWeight) }
    
    // Determine goal label based on comparison with current weight
    val goalLabel = when {
        selectedWeight < currentWeight - 1 -> "Lose Weight"
        selectedWeight > currentWeight + 1 -> "Gain Weight"
        else -> "Maintain Weight"
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Edit Weight Goal",
                        fontFamily = Inter,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(0.3f))
            
            // Goal label
            Text(
                text = goalLabel,
                fontFamily = Inter,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Weight display
            Text(
                text = String.format("%.1f lbs", selectedWeight),
                fontFamily = Inter,
                fontWeight = FontWeight.Bold,
                fontSize = 48.sp,
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Ruler scale picker
            RulerScalePickerWidget(
                value = selectedWeight,
                minValue = 88f,
                maxValue = 330f,
                onValueChange = { selectedWeight = it },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.weight(0.5f))
            
            // Done button
            Button(
                onClick = { onSave(selectedWeight) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(bottom = 0.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1C1C1E),
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Done",
                    fontFamily = Inter,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * Horizontal ruler scale picker widget with smooth LazyRow scrolling.
 * Uses native scroll physics for smooth fling behavior and snaps to nearest value.
 */
@Composable
private fun RulerScalePickerWidget(
    value: Float,
    minValue: Float,
    maxValue: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val itemWidthDp = 12.dp
    val itemWidthPx = with(density) { itemWidthDp.toPx() }
    
    // Generate weight values (0.5 lb increments for smooth selection)
    val stepSize = 0.5f
    val weights = remember(minValue, maxValue) {
        generateSequence(minValue) { it + stepSize }
            .takeWhile { it <= maxValue }
            .toList()
    }
    
    // Calculate initial index based on current value
    val initialIndex = remember(value, minValue, stepSize) {
        ((value - minValue) / stepSize).toInt().coerceIn(0, weights.size - 1)
    }
    
    val listState = androidx.compose.foundation.lazy.rememberLazyListState(
        initialFirstVisibleItemIndex = initialIndex
    )
    
    // Calculate the center offset for snapping
    BoxWithConstraints(modifier = modifier) {
        val halfScreenWidth = constraints.maxWidth / 2
        val centerItemCount = (halfScreenWidth / itemWidthPx).toInt()
        
        // Detect scroll stop and snap to center item
        LaunchedEffect(listState) {
            snapshotFlow { listState.isScrollInProgress }
                .collect { isScrolling ->
                    if (!isScrolling) {
                        // Get the item closest to center
                        val layoutInfo = listState.layoutInfo
                        val viewportCenter = layoutInfo.viewportStartOffset + layoutInfo.viewportSize.width / 2
                        
                        val centerItem = layoutInfo.visibleItemsInfo.minByOrNull { 
                            kotlin.math.abs((it.offset + it.size / 2) - viewportCenter)
                        }
                        
                        centerItem?.let { item ->
                            val targetIndex = item.index.coerceIn(0, weights.size - 1)
                            val newWeight = weights.getOrElse(targetIndex) { value }
                            if (newWeight != value) {
                                onValueChange(newWeight)
                            }
                        }
                    }
                }
        }
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Center indicator triangle
            Canvas(
                modifier = Modifier
                    .width(16.dp)
                    .height(12.dp)
            ) {
                val path = Path().apply {
                    moveTo(size.width / 2, size.height)
                    lineTo(0f, 0f)
                    lineTo(size.width, 0f)
                    close()
                }
                drawPath(path, Color(0xFF1C1C1E), style = Fill)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Ruler with LazyRow
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(Color(0xFFF8F8F8))
            ) {
                androidx.compose.foundation.lazy.LazyRow(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = (this@BoxWithConstraints.maxWidth / 2) - (itemWidthDp / 2)
                    ),
                    flingBehavior = androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior(listState)
                ) {
                    items(weights.size) { index ->
                        val itemWeight = weights[index]
                        val isMajor = (itemWeight * 10).toInt() % 50 == 0 // Every 5 lbs
                        val isMedium = (itemWeight * 10).toInt() % 10 == 0 // Every 1 lb
                        
                        Column(
                            modifier = Modifier
                                .width(itemWidthDp)
                                .fillMaxHeight(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            // Weight label for major ticks
                            if (isMajor) {
                                Text(
                                    text = itemWeight.toInt().toString(),
                                    fontFamily = Inter,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                            
                            // Tick mark
                            val tickHeight = when {
                                isMajor -> 36.dp
                                isMedium -> 24.dp
                                else -> 16.dp
                            }
                            val tickWidth = if (isMajor) 2.dp else 1.dp
                            val tickColor = when {
                                isMajor -> Color(0xFF1C1C1E)
                                isMedium -> Color(0xFF888888)
                                else -> Color(0xFFCCCCCC)
                            }
                            
                            Box(
                                modifier = Modifier
                                    .width(tickWidth)
                                    .height(tickHeight)
                                    .background(tickColor)
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
                
                // Center line indicator (overlay)
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .width(3.dp)
                        .height(60.dp)
                        .background(Color(0xFF1C1C1E))
                )
            }
        }
    }
}
