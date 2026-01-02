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
 * Set Birthday screen with Month, Day, Year wheel pickers.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBirthdayScreen(
    currentMonth: String = "January",
    currentDay: Int = 1,
    currentYear: Int = 2001,
    onBack: () -> Unit,
    onSave: (month: String, day: Int, year: Int) -> Unit
) {
    val months = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    val days = (1..31).toList()
    val years = (1940..2010).toList()
    
    var selectedMonth by remember { mutableStateOf(currentMonth) }
    var selectedDay by remember { mutableIntStateOf(currentDay) }
    var selectedYear by remember { mutableIntStateOf(currentYear) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Set Birthday",
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
            Spacer(modifier = Modifier.weight(0.3f))
            
            // Date picker area
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.7f),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Month picker
                BirthdayWheelPicker(
                    items = months,
                    selectedIndex = months.indexOf(selectedMonth).coerceAtLeast(0),
                    onSelectedIndexChange = { selectedMonth = months[it] },
                    modifier = Modifier.weight(1f)
                )
                
                // Day picker
                BirthdayWheelPicker(
                    items = days.map { String.format("%02d", it) },
                    selectedIndex = (selectedDay - 1).coerceIn(0, days.lastIndex),
                    onSelectedIndexChange = { selectedDay = days[it] },
                    modifier = Modifier.weight(0.6f)
                )
                
                // Year picker
                BirthdayWheelPicker(
                    items = years.map { it.toString() },
                    selectedIndex = (selectedYear - 1940).coerceIn(0, years.lastIndex),
                    onSelectedIndexChange = { selectedYear = years[it] },
                    modifier = Modifier.weight(0.8f)
                )
            }
            
            // Save button
            Button(
                onClick = { onSave(selectedMonth, selectedDay, selectedYear) },
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
 * Wheel picker component for birthday selection.
 */
@Composable
private fun BirthdayWheelPicker(
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
