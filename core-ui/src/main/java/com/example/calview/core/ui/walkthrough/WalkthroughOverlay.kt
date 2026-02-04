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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Restaurant
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
import androidx.compose.ui.layout.positionInRoot
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
    
    // Animate the spotlight coordinates for smooth transitions - speed up slightly
    val animatedLeft by animateFloatAsState(targetValue = activeRect.left, animationSpec = tween(200), label = "left")
    val animatedTop by animateFloatAsState(targetValue = activeRect.top, animationSpec = tween(200), label = "top")
    val animatedRight by animateFloatAsState(targetValue = activeRect.right, animationSpec = tween(200), label = "right")
    val animatedBottom by animateFloatAsState(targetValue = activeRect.bottom, animationSpec = tween(200), label = "bottom")
    
    val animatedRect = Rect(animatedLeft, animatedTop, animatedRight, animatedBottom)
    
    // Pulse animation for the spotlight
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.03f,
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
        val screenHeight = maxHeight.value * LocalDensity.current.density
        
        // Dimmed Background with Animated Spotlight
        Canvas(modifier = Modifier.fillMaxSize()) {
            val spotlightPath = Path().apply {
                if (animatedRect != Rect.Zero) {
                    val inflation = 6.dp.toPx() * pulseScale
                    addRoundRect(
                        RoundRect(
                            rect = animatedRect.inflate(inflation),
                            cornerRadius = CornerRadius(24.dp.toPx())
                        )
                    )
                }
            }

            clipPath(spotlightPath, clipOp = ClipOp.Difference) {
                drawRect(
                    color = Color.Black.copy(alpha = 0.75f),
                    size = size
                )
            }
        }

        // Tooltip UI - Adaptive positioning
        if (activeRect != Rect.Zero) {
            // Determine if the target is in the bottom half of the screen
            val targetCenterY = animatedRect.center.y
            val showAtTop = targetCenterY > screenHeight * 0.6f
            
            AnimatedContent(
                targetState = showAtTop,
                transitionSpec = {
                    if (targetState) {
                        // Sliding to top
                        (slideInVertically { height -> -height } + fadeIn(animationSpec = tween(400)))
                            .togetherWith(slideOutVertically { height -> height } + fadeOut(animationSpec = tween(400)))
                    } else {
                        // Sliding to bottom
                        (slideInVertically { height -> height } + fadeIn(animationSpec = tween(400)))
                            .togetherWith(slideOutVertically { height -> -height } + fadeOut(animationSpec = tween(400)))
                    }
                },
                label = "tooltipPosition",
                modifier = Modifier.fillMaxSize()
            ) { targetShowAtTop ->
                Box(modifier = Modifier.fillMaxSize()) {
                    TooltipContent(
                        step = currentStep,
                        onNext = onNext,
                        onSkip = onSkip,
                        isLastStep = currentStepIndex == steps.size - 1,
                        modifier = Modifier
                            .align(if (targetShowAtTop) Alignment.TopCenter else Alignment.BottomCenter)
                            .padding(16.dp)
                            .padding(
                                top = if (targetShowAtTop) 32.dp else 0.dp,
                                bottom = if (targetShowAtTop) 0.dp else 32.dp
                            )
                            .safeDrawingPadding() // Handle system bars
                    )
                }
            }
        }
    }
}

@Composable
private fun TooltipContent(
    step: WalkthroughStep,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    isLastStep: Boolean,
    modifier: Modifier = Modifier
) {
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
    
    Card(
        modifier = modifier
            .widthIn(max = 400.dp)
            .animateContentSize(animationSpec = tween(300)),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            // Glassmorphism effect: Semi-transparent surface
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Content area becomes scrollable ONLY if it exceeds max height
            // But we keep it simple here as text is usually short
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                shape = CircleShape,
                modifier = Modifier.size(40.dp)
            ) {
                val icon = when {
                    step.id.contains("nutrition", true) -> Icons.Default.PieChart
                    step.id.contains("score", true) -> Icons.Default.AutoAwesome
                    step.id.contains("water", true) -> Icons.Default.WaterDrop
                    step.id.contains("scan", true) -> Icons.Default.CameraAlt
                    step.id.contains("calendar", true) -> Icons.Default.CalendarToday
                    step.id.contains("health", true) -> Icons.Default.Favorite
                    step.id.contains("meal", true) -> Icons.Default.Restaurant
                    else -> Icons.Default.Info
                }
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = step.title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    fontSize = 18.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = step.description,
                style = MaterialTheme.typography.bodyMedium.copy(
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                    fontSize = 14.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(20.dp))

            // Actions area - pinned to the bottom of the card
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = safeOnSkip,
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text(
                        "Skip tour", 
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    )
                }
                
                Button(
                    onClick = safeOnNext,
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                    modifier = Modifier.height(44.dp)
                ) {
                    Text(
                        if (isLastStep) "Get Started" else "Next Step",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    )
                }
            }
        }
    }
}

// Extension to help capture coordinates
fun Modifier.onPositionedRect(onRect: (Rect) -> Unit): Modifier = this.onGloballyPositioned { coordinates ->
    val position = coordinates.positionInRoot()
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
