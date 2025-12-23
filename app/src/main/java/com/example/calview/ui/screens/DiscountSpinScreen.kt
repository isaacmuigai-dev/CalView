package com.example.calview.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun DiscountSpinScreen(
    onContinue: () -> Unit
) {
    var isSpinning by remember { mutableStateOf(false) }
    val rotation = remember { Animatable(0f) }

    LaunchedEffect(isSpinning) {
        if (isSpinning) {
            // Spin multiple full rotations (3600 degrees = 10 rotations) plus a random amount
            val randomStopAngle = (0..360).random().toFloat()
            rotation.animateTo(
                targetValue = rotation.value + 3600f + randomStopAngle,
                animationSpec = tween(
                    durationMillis = 3000,
                    easing = LinearOutSlowInEasing
                )
            )
            isSpinning = false
            onContinue()
        }
    }

    Scaffold(containerColor = Color.White) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Text(
                "Spin to unlock an\nexclusive discount",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                lineHeight = 40.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            // Spin Wheel
            Box(contentAlignment = Alignment.Center) {
                SpinWheel(rotation.value)

                // Pointer
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.CenterEnd)
                        .offset(x = 10.dp)
                        .rotate(180f),
                    tint = Color.Black
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { isSpinning = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                shape = RoundedCornerShape(32.dp),
                enabled = !isSpinning
            ) {
                Text(
                    if (isSpinning) "Spinning..." else "Continue",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun SpinWheel(rotation: Float) {
    Canvas(modifier = Modifier.size(300.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.width / 2
        val segments = listOf("50%", "70%", "🎁", "60%", "30%", "No luck")
        val sweepAngle = 360f / segments.size

        segments.forEachIndexed { index, label ->
            val color = if (index % 2 == 0) Color.Black else Color.White
            val textColor = if (index % 2 == 0) Color.White else Color.Black

            drawArc(
                color = color,
                startAngle = rotation + index * sweepAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                size = Size(size.width, size.height)
            )

            // Draw label
            val angle = rotation + index * sweepAngle + sweepAngle / 2
            val x = center.x + (radius * 0.7f) * cos(angle * PI / 180).toFloat()
            val y = center.y + (radius * 0.7f) * sin(angle * PI / 180).toFloat()

            drawContext.canvas.nativeCanvas.apply {
                save()
                rotate(angle + 90, x, y)
                val paint = android.graphics.Paint().apply {
                    this.color = textColor.toArgb()
                    this.textSize = 40f
                    this.textAlign = android.graphics.Paint.Align.CENTER
                    this.isFakeBoldText = true
                }
                drawText(label, x, y, paint)
                restore()
            }
        }

        // Inner Circle
        drawCircle(color = Color.White, radius = 30.dp.toPx(), center = center)
        drawCircle(color = Color.Black, radius = 25.dp.toPx(), center = center)
        // Logo center
        // drawImage(...) would go here
    }
}

@Preview(showBackground = true)
@Composable
fun DiscountSpinScreenPreview() {
    DiscountSpinScreen(onContinue = {})
}
