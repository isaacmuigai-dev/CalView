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
import com.example.calview.core.ui.theme.SpaceGroteskFontFamily
import com.example.calview.core.ui.theme.InterFontFamily
import kotlinx.coroutines.flow.collectLatest
import com.example.calview.feature.dashboard.R
import androidx.compose.ui.res.stringResource
import java.util.Locale

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
    var selectedWeight by remember { 
        mutableFloatStateOf(if (goalWeight > 0) goalWeight else currentWeight) 
    }
    
    // Determine goal label based on comparison with current weight
    // Determine goal label based on comparison with current weight
    val goalLabel = when {
        selectedWeight < currentWeight - 1 -> stringResource(R.string.lose_weight_label)
        selectedWeight > currentWeight + 1 -> stringResource(R.string.gain_weight_label)
        else -> stringResource(R.string.maintain_weight_label)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.edit_weight_goal_title),
                        fontFamily = InterFontFamily,
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
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
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
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Weight display
            Text(
                text = String.format(Locale.US, "%.1f kg", selectedWeight),
                fontFamily = SpaceGroteskFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 48.sp,
                letterSpacing = (-0.02).sp,
                color = MaterialTheme.colorScheme.onBackground
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
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = stringResource(R.string.done_action),
                    fontFamily = InterFontFamily,
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
    
    // Generate weight values (0.1 kg increments for smooth selection)
    val weights = remember {
        (200..4000).map { it / 10f } // 20.0 to 400.0 kg in 0.1 kg increments
    }
    
    // Calculate initial index based on current value
    val initialIndex = remember(value) {
         val index = weights.indexOfFirst { kotlin.math.abs(it - value) < 0.05f }
         if (index >= 0) index else weights.size / 2
    }
    
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = initialIndex
    )
    
    // Scroll to the correct position when value changes
    LaunchedEffect(value) {
        val targetIndex = weights.indexOfFirst { kotlin.math.abs(it - value) < 0.05f }
        if (targetIndex >= 0 && targetIndex != listState.firstVisibleItemIndex) {
            listState.scrollToItem(targetIndex)
        }
    }
    
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
            // Get color outside Canvas (Composable context)
            val indicatorColor = MaterialTheme.colorScheme.primary
            
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
                drawPath(path, indicatorColor, style = Fill)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Ruler with LazyRow
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
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
                        val isMajor = (itemWeight * 10).toInt() % 50 == 0 // Every 5 kg
                        val isMedium = (itemWeight * 10).toInt() % 10 == 0 // Every 1 kg
                        
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
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = (-0.02).sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}
