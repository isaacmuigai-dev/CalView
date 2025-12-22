package com.example.calview.feature.onboarding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calview.core.ui.components.CalAICard
import com.example.calview.core.ui.theme.Inter
import com.example.calview.feature.onboarding.components.OnboardingTemplate

@Composable
fun WeightTransitionScreen(
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    OnboardingTemplate(
        title = "You have great potential to crush your goal",
        progress = 0.25f,
        onBack = onBack,
        onContinue = onContinue
    ) {
        CalAICard {
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = "Your weight transition",
                    fontFamily = Inter,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.Black
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    TransitionChart()
                    
                    // Trophy icon at the end
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 10.dp, end = 10.dp)
                            .size(32.dp)
                            .background(Color(0xFFE5A87B), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("3 Days", fontSize = 14.sp, color = Color.Black, fontWeight = FontWeight.Medium)
                    Text("7 Days", fontSize = 14.sp, color = Color.Black, fontWeight = FontWeight.Medium)
                    Text("30 Days", fontSize = 14.sp, color = Color.Black, fontWeight = FontWeight.Medium)
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Based on Cal AI's historical data, weight loss is usually delayed at first, but after 7 days, you can burn fat like crazy!",
                    fontFamily = Inter,
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun TransitionChart() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        
        // Path for the area gradient
        val fillPath = Path().apply {
            moveTo(0f, height * 0.7f)
            cubicTo(
                width * 0.3f, height * 0.7f,
                width * 0.4f, height * 0.4f,
                width * 0.6f, height * 0.4f
            )
            cubicTo(
                width * 0.8f, height * 0.4f,
                width * 0.9f, height * 0.2f,
                width, height * 0.2f
            )
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }
        
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFE5A87B).copy(alpha = 0.3f),
                    Color.White.copy(alpha = 0.1f)
                )
            )
        )
        
        // Path for the line
        val linePath = Path().apply {
            moveTo(0f, height * 0.7f)
            cubicTo(
                width * 0.3f, height * 0.7f,
                width * 0.4f, height * 0.4f,
                width * 0.6f, height * 0.4f
            )
            cubicTo(
                width * 0.8f, height * 0.4f,
                width * 0.9f, height * 0.2f,
                width, height * 0.2f
            )
        }
        
        drawPath(
            path = linePath,
            color = Color(0xFF8B5E3C),
            style = Stroke(width = 3.dp.toPx())
        )
        
        // Points
        drawCircle(Color.White, radius = 6.dp.toPx(), center = Offset(width * 0.1f, height * 0.7f))
        drawCircle(Color.Black, radius = 6.dp.toPx(), center = Offset(width * 0.1f, height * 0.7f), style = Stroke(2.dp.toPx()))
        
        drawCircle(Color.White, radius = 6.dp.toPx(), center = Offset(width * 0.35f, height * 0.65f))
        drawCircle(Color.Black, radius = 6.dp.toPx(), center = Offset(width * 0.35f, height * 0.65f), style = Stroke(2.dp.toPx()))
        
        drawCircle(Color.White, radius = 6.dp.toPx(), center = Offset(width * 0.55f, height * 0.4f))
        drawCircle(Color.Black, radius = 6.dp.toPx(), center = Offset(width * 0.55f, height * 0.4f), style = Stroke(2.dp.toPx()))

        // Bottom axis line
        drawLine(
            color = Color.Black,
            start = Offset(0f, height),
            end = Offset(width, height),
            strokeWidth = 2.dp.toPx()
        )
    }
}
