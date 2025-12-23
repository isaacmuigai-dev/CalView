package com.example.calview.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calview.ui.theme.CalAIGreen

@Composable
fun CalorieRing(
    consumed: Int,
    goal: Int,
    modifier: Modifier = Modifier
) {
    val progress = (consumed.toFloat() / goal.toFloat()).coerceIn(0f, 1f)
    val remaining = (goal - consumed).coerceAtLeast(0)

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val strokeWidth = 12.dp
        val color = CalAIGreen
        val trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = remaining.toString(),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 48.sp
                )
            )
            Text(
                text = "Calories left",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}
