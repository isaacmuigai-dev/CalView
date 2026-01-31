package com.example.calview.feature.dashboard

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.*
import com.example.calview.core.ui.util.AdaptiveLayoutUtils
import com.example.calview.core.ui.util.LocalWindowSizeClass
import com.example.calview.feature.dashboard.R
import androidx.compose.ui.res.stringResource
import com.example.calview.core.ui.theme.SpaceGroteskFontFamily
import com.example.calview.core.ui.theme.InterFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FastingScreen(
    viewModel: FastingViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.fasting_timer_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        // Adaptive layout
        val windowSizeClass = LocalWindowSizeClass.current
        val horizontalPadding = AdaptiveLayoutUtils.getHorizontalPadding(windowSizeClass.widthSizeClass)
        val maxContentWidth = AdaptiveLayoutUtils.getMaxContentWidth(windowSizeClass.widthSizeClass)
        
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = maxContentWidth)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontalPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatCard(
                    label = stringResource(R.string.stat_completed),
                    value = "${uiState.completedFasts}",
                    icon = "✅"
                )
                StatCard(
                    label = stringResource(R.string.stat_current_streak),
                    value = "🔥 ${uiState.completedFasts}",
                    icon = ""
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Main Timer Circle
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(280.dp)
            ) {
                // Progress Ring
                CircularProgressRing(
                    progress = uiState.progress,
                    isActive = uiState.isActive,
                    modifier = Modifier.fillMaxSize()
                )
                
                // Center Content
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (uiState.isActive) {
                        Text(
                            text = formatTime(uiState.elapsedMinutes),
                            fontFamily = SpaceGroteskFontFamily,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = (-0.02).sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = stringResource(R.string.elapsed_label),
                            fontFamily = InterFontFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${formatTime(uiState.remainingMinutes)} ${stringResource(R.string.left_suffix)}",
                            fontFamily = SpaceGroteskFontFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = (-0.02).sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = uiState.selectedFastType.label,
                            fontFamily = SpaceGroteskFontFamily,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = (-0.02).sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = stringResource(R.string.fast_hour_suffix, uiState.selectedFastType.fastingHours),
                            fontFamily = InterFontFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Fast Type Selection (only when not active)
            if (!uiState.isActive) {
                Text(
                    text = stringResource(R.string.select_fast_window),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(FastType.entries) { type ->
                        FastTypeChip(
                            type = type,
                            isSelected = uiState.selectedFastType == type,
                            onClick = { viewModel.selectFastType(type) }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Action Buttons
            if (uiState.isActive) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Cancel Button
                    OutlinedButton(
                        onClick = { viewModel.cancelFast() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cancel_fast_desc))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.cancel_fast_desc))
                    }
                    
                    // End Fast Button
                    Button(
                        onClick = { viewModel.endFast() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (uiState.progress >= 1f) 
                                Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            if (uiState.progress >= 1f) Icons.Default.Check else Icons.Default.Stop,
                            contentDescription = if (uiState.progress >= 1f) stringResource(R.string.complete_fast_desc) else stringResource(R.string.stop_fast_desc)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (uiState.progress >= 1f) stringResource(R.string.complete_action) else stringResource(R.string.end_fast_action))
                    }
                }
            } else {
                // Start Button
                Button(
                    onClick = { viewModel.startFast() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = stringResource(R.string.start_fast_desc))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.start_fast_action), fontSize = 18.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Recent History
            if (uiState.recentSessions.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.recent_fasts_title),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                uiState.recentSessions.forEach { session ->
                    RecentFastCard(session = session)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
        } // Close Box
    }
}

@Composable
private fun CircularProgressRing(
    progress: Float,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(500),
        label = "progress"
    )
    
    val sweepAngle = animatedProgress * 360f
    
    Canvas(modifier = modifier) {
        val strokeWidth = 16.dp.toPx()
        val radius = (size.minDimension - strokeWidth) / 2
        val topLeft = Offset(
            (size.width - radius * 2) / 2,
            (size.height - radius * 2) / 2
        )
        
        // Background ring
        drawArc(
            color = Color.LightGray.copy(alpha = 0.3f),
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        
        // Progress ring
        if (isActive) {
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        Color(0xFF4CAF50),
                        Color(0xFF8BC34A),
                        Color(0xFFCDDC39),
                        Color(0xFF4CAF50)
                    )
                ),
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = topLeft,
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
private fun FastTypeChip(
    type: FastType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        color = if (isSelected) 
            MaterialTheme.colorScheme.primary 
        else 
            MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = type.label,
                fontFamily = SpaceGroteskFontFamily,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.02).sp,
                color = if (isSelected) 
                    MaterialTheme.colorScheme.onPrimary 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.fast_hour_suffix, type.fastingHours),
                fontFamily = InterFontFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) 
                    MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    icon: String
) {
    Card(
        modifier = Modifier.width(150.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontFamily = SpaceGroteskFontFamily,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.02).sp
            )
            Text(
                text = label,
                fontFamily = InterFontFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RecentFastCard(session: com.example.calview.core.data.local.FastingSessionEntity) {
    val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
    val duration = session.endTime?.let { 
        ((it - session.startTime) / 60000).toInt() 
    } ?: 0
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = dateFormat.format(Date(session.startTime)),
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = session.fastingType,
                    fontFamily = InterFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = formatTime(duration),
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.02).sp
                )
                if (session.isCompleted) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun formatTime(minutes: Int): String {
    val hours = minutes / 60
    val mins = minutes % 60
    return if (hours > 0) {
        stringResource(R.string.time_hours_mins, hours, mins)
    } else {
        stringResource(R.string.time_mins, mins)
    }
}
