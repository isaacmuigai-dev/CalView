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
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Set Height & Weight screen with Imperial/Metric toggle and wheel pickers.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditHeightWeightScreen(
    currentHeightCm: Int = 168,
    currentWeightLbs: Float = 119f,
    onBack: () -> Unit,
    onSave: (heightCm: Int, weightLbs: Float) -> Unit
) {
    var isMetric by remember { mutableStateOf(false) }
    
    // Height in imperial
    var heightFeet by remember { mutableIntStateOf(currentHeightCm / 30) }
    var heightInches by remember { mutableIntStateOf((currentHeightCm % 30) / 2) }
    
    // Height in metric
    var heightCm by remember { mutableIntStateOf(currentHeightCm) }
    
    // Weight
    var weightLb by remember { mutableIntStateOf(currentWeightLbs.toInt()) }
    var weightKg by remember { mutableIntStateOf((currentWeightLbs / 2.205f).toInt()) }
    
    // Initialize from current values
    LaunchedEffect(currentHeightCm) {
        val totalInches = (currentHeightCm / 2.54).toInt()
        heightFeet = totalInches / 12
        heightInches = totalInches % 12
        heightCm = currentHeightCm
    }
    
    LaunchedEffect(currentWeightLbs) {
        weightLb = currentWeightLbs.toInt()
        weightKg = (currentWeightLbs / 2.205f).toInt()
    }
    
    // Calculate final values
    val finalHeightCm = if (isMetric) {
        heightCm
    } else {
        ((heightFeet * 12 + heightInches) * 2.54).toInt()
    }
    
    val finalWeightLbs = if (isMetric) {
        weightKg * 2.205f
    } else {
        weightLb.toFloat()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Set Height & Weight",
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
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
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
                    onCheckedChange = { isMetric = it },
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
                    WheelPickerWidget(
                        items = (100..220).map { "$it cm" },
                        selectedIndex = (heightCm - 100).coerceIn(0, 120),
                        onSelectedIndexChange = { heightCm = it + 100 },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    // Imperial: feet and inches pickers
                    WheelPickerWidget(
                        items = (3..8).map { "$it ft" },
                        selectedIndex = (heightFeet - 3).coerceIn(0, 5),
                        onSelectedIndexChange = { heightFeet = it + 3 },
                        modifier = Modifier.weight(0.5f)
                    )
                    
                    WheelPickerWidget(
                        items = (0..11).map { "$it in" },
                        selectedIndex = heightInches.coerceIn(0, 11),
                        onSelectedIndexChange = { heightInches = it },
                        modifier = Modifier.weight(0.5f)
                    )
                }
                
                // Weight picker
                if (isMetric) {
                    WheelPickerWidget(
                        items = (30..200).map { "$it kg" },
                        selectedIndex = (weightKg - 30).coerceIn(0, 170),
                        onSelectedIndexChange = { weightKg = it + 30 },
                        modifier = Modifier.weight(0.5f)
                    )
                } else {
                    WheelPickerWidget(
                        items = (66..440).map { "$it lb" },
                        selectedIndex = (weightLb - 66).coerceIn(0, 374),
                        onSelectedIndexChange = { weightLb = it + 66 },
                        modifier = Modifier.weight(0.5f)
                    )
                }
            }
            
            // Save button
            Button(
                onClick = { onSave(finalHeightCm, finalWeightLbs) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1C1C1E),
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Save changes",
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
