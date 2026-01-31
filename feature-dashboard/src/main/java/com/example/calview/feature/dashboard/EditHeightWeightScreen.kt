package com.example.calview.feature.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calview.core.ui.theme.Inter
import com.example.calview.core.ui.theme.SpaceGroteskFontFamily
import com.example.calview.core.ui.theme.InterFontFamily
import kotlinx.coroutines.launch
import kotlin.math.abs
import com.example.calview.feature.dashboard.R
import androidx.compose.ui.res.stringResource

/**
 * Set Height & Weight screen with metric units only (cm/kg).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditHeightWeightScreen(
    currentHeightCm: Int = 168,
    currentWeightKg:Float = 68f,
    onBack: () -> Unit,
    onSave: (heightCm: Int, weightKg: Float) -> Unit
) {
    // Metric-only app (kg/cm)
    var heightCm by remember { mutableIntStateOf(currentHeightCm) }
    var weightKg by remember { mutableIntStateOf(currentWeightKg.toInt()) }
    

    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.set_height_weight_title),
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
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            Spacer(modifier = Modifier.height(32.dp))
            
            
            // Column headers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = stringResource(R.string.height_header),
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = stringResource(R.string.weight_header),
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground,
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
                // Metric cm picker
                WheelPickerWidget(
                    items = (100..220).map { "$it cm" },
                    selectedIndex = (heightCm - 100).coerceIn(0, 120),
                    onSelectedIndexChange = { heightCm = it + 100 },
                    modifier = Modifier.weight(1f)
                )
                
                // Metric kg picker
                WheelPickerWidget(
                    items = (30..200).map { "$it kg" },
                    selectedIndex = (weightKg - 30).coerceIn(0, 170),
                    onSelectedIndexChange = { weightKg = it + 30 },
                    modifier = Modifier.weight(0.5f)
                )
            }
            
            // Save button
            Button(
                onClick = { onSave(heightCm, weightKg.toFloat()) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = stringResource(R.string.save_changes_button),
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
 * Wheel picker component with scroll snapping.
 */
@Composable
private fun WheelPickerWidget(
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
        listState.scrollToItem(selectedIndex.coerceIn(0, items.lastIndex))
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
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
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
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = if (index == selectedIndex) FontWeight.SemiBold else FontWeight.Normal,
                        fontSize = if (index == selectedIndex) 18.sp else 16.sp,
                        letterSpacing = if (index == selectedIndex) (-0.02).sp else 0.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = alpha),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
