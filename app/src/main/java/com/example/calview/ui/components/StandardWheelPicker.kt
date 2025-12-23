package com.example.calview.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StandardWheelPicker(
    items: List<String>,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    initialIndex: Int = 0,
    visibleItemsCount: Int = 5
) {
    val itemHeight = 48.dp
    val listState = rememberLazyListState(initialIndex)
    val snapFlingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val centerIndex = listState.firstVisibleItemIndex
            // Actually it might need an offset to be perfectly centered, but let's simplify
            onItemSelected(centerIndex)
        }
    }

    Box(
        modifier = modifier
            .height(itemHeight * visibleItemsCount)
            .width(100.dp),
        contentAlignment = Alignment.Center
    ) {
        // Selection Highlight
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight)
                .background(Color(0xFFF3F3F3), RoundedCornerShape(12.dp))
        )

        LazyColumn(
            state = listState,
            flingBehavior = snapFlingBehavior,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = itemHeight * (visibleItemsCount / 2))
        ) {
            items(items.size) { index ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = items[index],
                        fontSize = 20.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                }
            }
        }
    }
}
