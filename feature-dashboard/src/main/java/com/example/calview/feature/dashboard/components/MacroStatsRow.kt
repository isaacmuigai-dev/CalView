package com.example.calview.feature.dashboard.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MacroGaugeCard(
            label = "Protein",
            goalValue = protein,
            consumedValue = proteinConsumed,
            showEaten = showEaten,
            progress = if (protein > 0) proteinConsumed.toFloat() / protein else 0f,
            color = Color(0xFFD64D50),
            icon = Icons.Default.Favorite,
            modifier = Modifier.weight(1f)
        )
        MacroGaugeCard(
            label = "Carbs",
            goalValue = carbs,
            consumedValue = carbsConsumed,
            showEaten = showEaten,
            progress = if (carbs > 0) carbsConsumed.toFloat() / carbs else 0f,
            color = Color(0xFFE5A87B),
            icon = Icons.Default.LocalFlorist,
            modifier = Modifier.weight(1f)
        )
        MacroGaugeCard(
            label = "Fats",
            goalValue = fats,
            consumedValue = fatsConsumed,
            showEaten = showEaten,
            progress = if (fats > 0) fatsConsumed.toFloat() / fats else 0f,
            color = Color(0xFF6A8FB3),
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
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    val remaining = goalValue - consumedValue
    
    CalAICard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
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
                        // "Eaten" view: shows consumed/goal
                        Text(
                            text = buildAnnotatedString {
                                withStyle(SpanStyle(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )) {
                                    append(consumedValue.toString())
                                }
                                withStyle(SpanStyle(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = Color.Gray
                                )) {
                                    append(" /${goalValue}g")
                                }
                            }
                        )
                        Text(
                            text = buildAnnotatedString {
                                append("$label ")
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("eaten")
                                }
                            },
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    } else {
                        // "Left" view: shows remaining only
                        Text(
                            text = "${remaining}g",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = buildAnnotatedString {
                                append("$label ")
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("left")
                                }
                            },
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawArc(
                        color = Color(0xFFF3F3F3),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = color,
                        startAngle = -90f,
                        sweepAngle = 360f * progress.coerceIn(0f, 1f),
                        useCenter = false,
                        style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

