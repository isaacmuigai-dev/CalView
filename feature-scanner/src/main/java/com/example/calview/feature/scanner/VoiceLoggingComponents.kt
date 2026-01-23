package com.example.calview.feature.scanner

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.example.calview.core.data.local.MealEntity
import com.example.calview.core.ai.voice.VoiceState
import com.example.calview.core.ui.components.PremiumBadge

/**
 * Voice Logging FAB button for the scanner screen.
 * Premium feature for hands-free meal logging.
 */
@Composable
fun VoiceLoggingButton(
    isPremium: Boolean,
    isListening: Boolean,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    onUpgradeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (isGranted && isPremium) {
            onStartListening()
        }
    }
    
    // Pulsing animation when listening
    val infiniteTransition = rememberInfiniteTransition(label = "voice_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.2f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    FloatingActionButton(
        onClick = {
            when {
                !isPremium -> onUpgradeClick()
                !hasPermission -> permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                isListening -> onStopListening()
                else -> onStartListening()
            }
        },
        modifier = modifier
            .scale(if (isListening) scale else 1f)
            .semantics {
                contentDescription = if (isPremium) {
                    if (isListening) "Stop voice logging" else "Start voice logging"
                } else {
                    "Voice logging. Premium feature. Tap to upgrade."
                }
                stateDescription = if (isListening) "Listening" else "Ready"
                role = androidx.compose.ui.semantics.Role.Button
            },
        containerColor = if (isListening) Color(0xFFE53935) else MaterialTheme.colorScheme.secondary,
        shape = CircleShape
    ) {
        if (!isPremium) {
            Box {
                Icon(
                    Icons.Default.Mic,
                    contentDescription = "Voice Logging",
                    tint = Color.White
                )
                PremiumBadge(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 8.dp, y = (-8).dp)
                )
            }
        } else {
            Icon(
                imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                contentDescription = if (isListening) "Stop listening" else "Start voice logging",
                tint = Color.White
            )
        }
    }
}

/**
 * Voice Logging Dialog that shows the listening state and transcript.
 */
@Composable
fun VoiceLoggingDialog(
    voiceState: VoiceState,
    parsedMeals: List<MealEntity>,
    onDismiss: () -> Unit,
    onConfirmMeals: (List<MealEntity>) -> Unit,
    onRetry: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .semantics {
                    paneTitle = "Voice Meal Logging"
                    isTraversalGroup = true
                },
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .semantics { liveRegion = LiveRegionMode.Polite }, // Announce updates automatically
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (voiceState) {
                    is VoiceState.Idle -> {
                        ListeningAnimation()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Tap the mic to start",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    is VoiceState.Listening -> {
                        ListeningAnimation(isActive = true)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Listening...",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Say what you ate",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    is VoiceState.PartialResult -> {
                        ListeningAnimation(isActive = true)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "\"${voiceState.text}\"",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    is VoiceState.Processing -> {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(48.dp)
                                .semantics { contentDescription = "Processing your request with AI" },
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Processing with AI...",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    is VoiceState.Result -> {
                        if (parsedMeals.isNotEmpty()) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Success",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Found ${parsedMeals.size} item(s)",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Show parsed meals
                            parsedMeals.forEach { meal ->
                                MealPreviewCard(meal)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = onRetry,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Try Again")
                                }
                                Button(
                                    onClick = { onConfirmMeals(parsedMeals) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Add All")
                                }
                            }
                        } else {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = Color(0xFFFF9800),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Couldn't parse foods",
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Button(onClick = onRetry) {
                                Text("Try Again")
                            }
                        }
                    }
                    
                    is VoiceState.Error -> {
                        Icon(
                            Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = Color(0xFFE53935),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            voiceState.message,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onRetry) {
                            Text("Try Again")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ListeningAnimation(isActive: Boolean = false) {
    val infiniteTransition = rememberInfiniteTransition(label = "listening")
    val scale1 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = if (isActive) 1.4f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale1"
    )
    val scale2 by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = if (isActive) 1.2f else 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale2"
    )
    
    Box(contentAlignment = Alignment.Center) {
        // Outer ring
        Box(
            modifier = Modifier
                .size(80.dp)
                .scale(scale2)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    shape = CircleShape
                )
        )
        // Inner ring
        Box(
            modifier = Modifier
                .size(60.dp)
                .scale(scale1)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                    shape = CircleShape
                )
        )
        // Mic icon
        Icon(
            Icons.Default.Mic,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
private fun MealPreviewCard(meal: MealEntity) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    meal.name,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${meal.calories} cal",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "P:${meal.protein}g C:${meal.carbs}g F:${meal.fats}g",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
