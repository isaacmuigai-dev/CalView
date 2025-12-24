package com.example.calview.feature.dashboard.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calview.core.ui.components.CalAICard

@Composable
fun MacroStatsRow(
    protein: Int,
    carbs: Int,
    fats: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MacroGaugeCard(
            label = "Protein left",
            value = "${protein}g",
            progress = 0.5f,
            color = Color(0xFFD64D50),
            icon = Icons.Default.Favorite,
            modifier = Modifier.weight(1f)
        )
        MacroGaugeCard(
            label = "Carbs left",
            value = "${carbs}g",
            progress = 0.6f,
            color = Color(0xFFE5A87B),
            icon = Icons.Default.LocalFlorist,
            modifier = Modifier.weight(1f)
        )
        MacroGaugeCard(
            label = "Fats left",
            value = "${fats}g",
            progress = 0.4f,
            color = Color(0xFF6A8FB3),
            icon = Icons.Default.WaterDrop,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun MacroGaugeCard(
    label: String,
    value: String,
    progress: Float,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    CalAICard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )
            
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
                        sweepAngle = 360f * progress,
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
