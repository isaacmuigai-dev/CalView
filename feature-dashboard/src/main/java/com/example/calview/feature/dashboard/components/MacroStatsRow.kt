package com.example.calview.feature.dashboard.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calview.core.ui.components.CalAICard

@Composable
fun MacroStatsRow(
    protein: Int,
    carbs: Int,
    fats: Int,
    proteinConsumed: Int = 0,
    carbsConsumed: Int = 0,
    fatsConsumed: Int = 0
) {
    // Shared toggle state for all macro cards
    var showEaten by remember { mutableStateOf(true) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showEaten = !showEaten },
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MacroGaugeCard(
            label = "Protein",
            goalValue = protein,
            consumedValue = proteinConsumed,
            showEaten = showEaten,
            progress = if (protein > 0) proteinConsumed.toFloat() / protein else 0f,
            color = Color(0xFFE57373), // Light red/coral
            trackColor = Color(0xFF3D2C2C), // Dark reddish track
            icon = Icons.Default.Favorite,
            modifier = Modifier.weight(1f)
        )
        MacroGaugeCard(
            label = "Carbs",
            goalValue = carbs,
            consumedValue = carbsConsumed,
            showEaten = showEaten,
            progress = if (carbs > 0) carbsConsumed.toFloat() / carbs else 0f,
            color = Color(0xFFE5A87B), // Orange/wheat color
            trackColor = Color(0xFF3D3328), // Dark orange track
            icon = Icons.Default.LocalFlorist,
            modifier = Modifier.weight(1f)
        )
        MacroGaugeCard(
            label = "Fats",
            goalValue = fats,
            consumedValue = fatsConsumed,
            showEaten = showEaten,
            progress = if (fats > 0) fatsConsumed.toFloat() / fats else 0f,
            color = Color(0xFF64B5F6), // Light blue
            trackColor = Color(0xFF2C3340), // Dark blue track
            icon = Icons.Default.WaterDrop,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun MacroGaugeCard(
    label: String,
    goalValue: Int,
    consumedValue: Int,
    showEaten: Boolean,
    progress: Float,
    color: Color,
    trackColor: Color = Color(0xFFE0E0E0),
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    val remaining = goalValue - consumedValue
    
    // Animate progress changes
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(500),
        label = "progress"
    )
    
    CalAICard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // Animated content transition between eaten/left
            AnimatedContent(
                targetState = showEaten,
                transitionSpec = {
                    (slideInVertically { height -> height } + fadeIn(animationSpec = tween(300)))
                        .togetherWith(slideOutVertically { height -> -height } + fadeOut(animationSpec = tween(300)))
                },
                label = "macro_animation"
            ) { isEaten ->
                Column {
                    if (isEaten) {
                        // "Eaten" view: shows consumed/goal - matching reference exactly
                        Text(
                            text = buildAnnotatedString {
                                withStyle(SpanStyle(
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )) {
                                    append(consumedValue.toString())
                                }
                                withStyle(SpanStyle(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = Color.Gray
                                )) {
                                    append(" /${goalValue}g")
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = buildAnnotatedString {
                                withStyle(SpanStyle(
                                    fontWeight = FontWeight.Normal,
                                    color = Color.Gray
                                )) {
                                    append("$label ")
                                }
                                withStyle(SpanStyle(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )) {
                                    append("eaten")
                                }
                            },
                            fontSize = 13.sp
                        )
                    } else {
                        // "Left" view: shows remaining only - matching reference exactly
                        Text(
                            text = "${remaining}g",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = buildAnnotatedString {
                                withStyle(SpanStyle(
                                    fontWeight = FontWeight.Normal,
                                    color = Color.Gray
                                )) {
                                    append("$label ")
                                }
                                withStyle(SpanStyle(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )) {
                                    append("left")
                                }
                            },
                            fontSize = 13.sp
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Circular progress indicator with icon - enhanced design
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                // Progress ring canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Track (background circle)
                    drawArc(
                        color = trackColor,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
                    )
                    // Progress arc
                    drawArc(
                        color = color,
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress,
                        useCenter = false,
                        style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                // Icon in center
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}


