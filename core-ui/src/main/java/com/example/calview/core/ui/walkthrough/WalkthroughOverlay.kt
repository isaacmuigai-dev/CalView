package com.example.calview.core.ui.walkthrough

import androidx.compose.animation.*
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex

data class WalkthroughStep(
    val id: String,
    val title: String,
    val description: String,
    val targetRect: Rect? = null
)

@Composable
fun WalkthroughOverlay(
    steps: List<WalkthroughStep>,
    currentStepIndex: Int,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (currentStepIndex < 0 || currentStepIndex >= steps.size) return

    val currentStep = steps[currentStepIndex]
    
    // Animate the spotlight coordinates for smooth transitions
    val activeRect = currentStep.targetRect ?: Rect.Zero
    
    val animatedLeft by animateFloatAsState(targetValue = activeRect.left, animationSpec = tween(300), label = "left")
    val animatedTop by animateFloatAsState(targetValue = activeRect.top, animationSpec = tween(300), label = "top")
    val animatedRight by animateFloatAsState(targetValue = activeRect.right, animationSpec = tween(300), label = "right")
    val animatedBottom by animateFloatAsState(targetValue = activeRect.bottom, animationSpec = tween(300), label = "bottom")
    
    val animatedRect = Rect(animatedLeft, animatedTop, animatedRight, animatedBottom)
    
    // Pulse animation for the spotlight - slightly faster pulse
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .zIndex(1000f)
            .background(Color.Transparent)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { /* Disable background interaction */ }
    ) {
        val screenHeightPx = constraints.maxHeight.toFloat()
        
        // Dimmed Background with Animated Spotlight
        Canvas(modifier = Modifier.fillMaxSize()) {
            val spotlightPath = Path().apply {
                if (animatedRect != Rect.Zero) {
                    val inflation = 8.dp.toPx() * pulseScale
                    addRoundRect(
                        RoundRect(
                            rect = animatedRect.inflate(inflation),
                            cornerRadius = CornerRadius(20.dp.toPx())
                        )
                    )
                }
            }

            clipPath(spotlightPath, clipOp = ClipOp.Difference) {
                drawRect(
                    color = Color.Black.copy(alpha = 0.8f),
                    size = size
                )
            }
        }

        // Tooltip UI
        if (activeRect != Rect.Zero) {
            TooltipContent(
                step = currentStep,
                onNext = onNext,
                onSkip = onSkip,
                isLastStep = currentStepIndex == steps.size - 1,
                targetRect = activeRect,
                screenHeightPx = screenHeightPx,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
private fun TooltipContent(
    step: WalkthroughStep,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    isLastStep: Boolean,
    targetRect: Rect,
    screenHeightPx: Float,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val tooltipAbove = targetRect.center.y > screenHeightPx / 2
    
    // Debounce to prevent rapid jumping
    var lastClickTime by remember { mutableLongStateOf(0L) }
    val debounceMs = 500L
    
    val safeOnNext = {
        val now = System.currentTimeMillis()
        if (now - lastClickTime > debounceMs) {
            lastClickTime = now
            onNext()
        }
    }
    
    val safeOnSkip = {
        val now = System.currentTimeMillis()
        if (now - lastClickTime > debounceMs) {
            lastClickTime = now
            onSkip()
        }
    }
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Card(
            modifier = modifier
                .align(if (tooltipAbove) Alignment.BottomCenter else Alignment.TopCenter)
                .padding(
                    bottom = if (tooltipAbove) {
                        with(density) { (screenHeightPx - targetRect.top + 16.dp.toPx()).toDp() }
                    } else 0.dp,
                    top = if (!tooltipAbove) {
                        with(density) { (targetRect.bottom + 16.dp.toPx()).toDp() }
                    } else 0.dp
                )
                .widthIn(max = 340.dp)
                .heightIn(max = screenHeightPx.let { with(density) { (it * 0.4f).toDp() } }.coerceAtLeast(300.dp))
                .animateContentSize(animationSpec = tween(300)),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Content area - No weight or fixed scroll here unless absolutely necessary
                // This allows the card to grow/shrink based on text
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    shape = CircleShape,
                    modifier = Modifier.size(48.dp)
                ) {
                    val icon = when {
                        step.id.contains("nutrition", true) -> Icons.Default.PieChart
                        step.id.contains("score", true) -> Icons.Default.AutoAwesome
                        step.id.contains("water", true) -> Icons.Default.WaterDrop
                        step.id.contains("scan", true) -> Icons.Default.CameraAlt
                        else -> Icons.Default.Info
                    }
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = step.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(10.dp))
                
                Text(
                    text = step.description,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(24.dp))

                // Actions area
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = safeOnSkip) {
                        Text(
                            "Skip tour", 
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                    
                    Button(
                        onClick = safeOnNext,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp)
                    ) {
                        Text(
                            if (isLastStep) "Get Started" else "Next",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}

// Extension to help capture coordinates
fun Modifier.onPositionedRect(onRect: (Rect) -> Unit): Modifier = this.onGloballyPositioned { coordinates ->
    val position = coordinates.positionInWindow()
    val size = coordinates.size
    onRect(
        Rect(
            left = position.x,
            top = position.y,
            right = position.x + size.width,
            bottom = position.y + size.height
        )
    )
}
