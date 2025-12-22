package com.example.calview.feature.onboarding

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
            rotation.animateTo(
                targetValue = rotation.value + 3600f + (0..360).random().toFloat(),
                animationSpec = infiniteRepeatable(
                    animation = tween(3000, easing = LinearOutSlowInEasing),
                    repeatMode = RepeatMode.Restart
                )
            )
            // For demo purpose, just animating once
            rotation.animateTo(
                targetValue = rotation.value + 3600f + 144f, // Stops at 80% if calculated
                animationSpec = tween(3000, easing = LinearOutSlowInEasing)
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

@Composable
fun OneTimeOfferScreen(
    onStartTrial: () -> Unit,
    onClose: () -> Unit
) {
    var freeTrialEnabled by remember { mutableStateOf(true) }
    
    Scaffold(containerColor = Color.White) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = onClose, modifier = Modifier.align(Alignment.TopStart)) {
                    Icon(Icons.Default.Close, null)
                }
                Text(
                    "Your one-time offer",
                    modifier = Modifier.align(Alignment.Center),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Banner
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                color = Color.Black,
                shape = RoundedCornerShape(24.dp),
                shadowElevation = 8.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        "80% OFF\nFOREVER",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "KES4,500.00",
                fontSize = 24.sp,
                color = Color.LightGray,
                style = MaterialTheme.typography.bodyLarge.copy(
                    textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                )
            )
            Text(
                text = "KES250.00\n/mo",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFD64D50),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            HighlightItem("☕", "Less than a coffee.")
            HighlightItem("⚠️", "Close this screen? This price is gone")
            HighlightItem("🙋", "What are you waiting for?")
            
            Spacer(modifier = Modifier.weight(1f))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Free Trial Enabled", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Switch(
                    checked = freeTrialEnabled,
                    onCheckedChange = { freeTrialEnabled = it },
                    colors = SwitchDefaults.colors(checkedTrackColor = Color.Black)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Surface(
                modifier = Modifier.fillMaxWidth(),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                         Text("Yearly Plan", fontWeight = FontWeight.Bold)
                         Text("KES250.00 /mo", fontWeight = FontWeight.Bold)
                    }
                    Text("12mo • KES3,000.00", color = Color.Gray, fontSize = 14.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onStartTrial,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Start Free Trial", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                Text(" No Commitment - Cancel Anytime", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun HighlightItem(emoji: String, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(emoji, fontSize = 24.sp)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

private fun Color.toArgb(): Int {
    return (this.alpha * 255.0f + 0.5f).toInt() shl 24 or
            ((this.red * 255.0f + 0.5f).toInt() shl 16) or
            ((this.green * 255.0f + 0.5f).toInt() shl 8) or
            (this.blue * 255.0f + 0.5f).toInt()
}
