package com.example.calview.feature.onboarding

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calview.core.ui.theme.Inter
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay

/**
 * Congratulations your custom plan is ready screen.
 * Shows:
 * - Daily recommendation cards (Calories, Carbs, Protein, Fats) with animated rings
 * - Health score with animated progress bar
 * - How to reach your goals tips
 * - Medical sources list
 */
@Composable
fun CongratulationsScreen(
    currentStep: Int,
    totalSteps: Int,
    goal: String,
    currentWeight: Float, // in kg
    targetWeight: Float,  // in kg (goal weight)
    weeklyPace: Float,    // in kg/week
    recommendedCalories: Int,
    recommendedCarbs: Int,
    recommendedProtein: Int,
    recommendedFats: Int,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    val scrollState = rememberScrollState()
    
    // Calculate health score based on macro distribution
    // A balanced diet gets higher score
    val healthScore = remember(recommendedCalories, recommendedCarbs, recommendedProtein, recommendedFats) {
        calculateHealthScore(
            calories = recommendedCalories,
            carbs = recommendedCarbs,
            protein = recommendedProtein,
            fats = recommendedFats,
            goal = goal
        )
    }
    
    // Animation state - trigger animations on screen appearance
    var startAnimation by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(300) // Small delay for screen to settle
        startAnimation = true
    }
    
    // Animated health score
    val animatedHealthScore by animateFloatAsState(
        targetValue = if (startAnimation) healthScore.toFloat() else 0f,
        animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
        label = "health_score_animation"
    )
    
    // Determine goal action word
    val actionWord = when (goal) {
        "Lose Weight" -> "Lose"
        "Gain Weight" -> "Gain"
        else -> "Maintain"
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            LinearProgressIndicator(
                progress = { currentStep.toFloat() / totalSteps },
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            
            Spacer(modifier = Modifier.width(40.dp))
        }
        
        // Scrollable content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Complete",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Congratulations title
            Text(
                text = "Congratulations\nyour custom plan is ready!",
                fontFamily = Inter,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                lineHeight = 36.sp,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Target info - Calculate dynamic values
            if (goal != "Maintain") {
                // Calculate weight difference
                val weightDiff = kotlin.math.abs(targetWeight - currentWeight).toInt()
                
                // Calculate weeks to reach goal based on weekly pace (kg/week)
                val weeksToGoal = if (weeklyPace > 0) (weightDiff / weeklyPace).toInt() else 12
                
                // Calculate target date
                val targetDate = LocalDate.now().plusWeeks(weeksToGoal.toLong())
                val targetDateStr = targetDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
                
                // Goal Progress Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Your Goal Journey",
                            fontFamily = Inter,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Current Weight → Goal Weight
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Current Weight
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Current",
                                    fontFamily = Inter,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${currentWeight.toInt()} kg",
                                    fontFamily = Inter,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            
                            // Arrow
                            Text(
                                text = if (goal == "Lose Weight") "→" else "→",
                                fontSize = 24.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            
                            // Goal Weight
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Goal",
                                    fontFamily = Inter,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${targetWeight.toInt()} kg",
                                    fontFamily = Inter,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Details Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // Weekly Pace
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "⚡ Weekly Pace",
                                    fontFamily = Inter,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${"%.1f".format(weeklyPace)} kg",
                                    fontFamily = Inter,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            
                            // To ${actionWord}
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "📊 To $actionWord",
                                    fontFamily = Inter,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "$weightDiff kg",
                                    fontFamily = Inter,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            
                            // Time Estimate
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "⏱️ Estimated",
                                    fontFamily = Inter,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "$weeksToGoal weeks",
                                    fontFamily = Inter,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Target Date
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🎯 Estimated Goal Date: ",
                                    fontFamily = Inter,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = targetDateStr,
                                    fontFamily = Inter,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            // Daily Recommendation section
            Text(
                text = "Daily Recommendation",
                fontFamily = Inter,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Text(
                text = "You can edit this any time",
                fontFamily = Inter,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Macro cards grid with animations
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AnimatedMacroCard(
                    title = "Calories",
                    value = recommendedCalories.toString(),
                    emoji = "🔥",
                    color = MaterialTheme.colorScheme.primary,
                    progress = 1f, // Calories always shows full ring
                    startAnimation = startAnimation,
                    animationDelay = 0,
                    modifier = Modifier.weight(1f)
                )
                AnimatedMacroCard(
                    title = "Carbs",
                    value = "${recommendedCarbs}g",
                    emoji = "🌾",
                    color = Color(0xFFFF9800),
                    progress = calculateMacroProgress(recommendedCarbs, recommendedCalories, "carbs"),
                    startAnimation = startAnimation,
                    animationDelay = 100,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AnimatedMacroCard(
                    title = "Protein",
                    value = "${recommendedProtein}g",
                    emoji = "🥩",
                    color = Color(0xFFE57373),
                    progress = calculateMacroProgress(recommendedProtein, recommendedCalories, "protein"),
                    startAnimation = startAnimation,
                    animationDelay = 200,
                    modifier = Modifier.weight(1f)
                )
                AnimatedMacroCard(
                    title = "Fats",
                    value = "${recommendedFats}g",
                    emoji = "🧈",
                    color = Color(0xFF64B5F6),
                    progress = calculateMacroProgress(recommendedFats, recommendedCalories, "fats"),
                    startAnimation = startAnimation,
                    animationDelay = 300,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Health Score card with animation
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Heart icon with pulse animation
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFFCE4EC),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(text = "💗", fontSize = 20.sp)
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Health score",
                            fontFamily = Inter,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Animated progress bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(animatedHealthScore / 10f)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        when {
                                            animatedHealthScore >= 7 -> Color(0xFF4CAF50) // Green
                                            animatedHealthScore >= 5 -> Color(0xFFFF9800) // Orange
                                            else -> Color(0xFFF44336) // Red
                                        }
                                    )
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Text(
                        text = "${animatedHealthScore.toInt()}/10",
                        fontFamily = Inter,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // How to reach your goals section
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "How to reach your goals:",
                        fontFamily = Inter,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    GoalTip(
                        emoji = "💗",
                        text = "Get your weekly life score and improve your routine."
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    GoalTip(
                        emoji = "🥑",
                        text = "Track your food"
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    GoalTip(
                        emoji = "🔥",
                        text = "Follow your daily calorie recommendation"
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    GoalTip(
                        emoji = "⚖️",
                        text = "Balance your carbs, protein, fat"
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Medical sources
            Text(
                text = "Plan based on the following sources, among other peer-reviewed medical studies:",
                fontFamily = Inter,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground,
                lineHeight = 20.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Column {
                SourceItem("Basal metabolic rate")
                SourceItem("Calorie counting - Harvard")
                SourceItem("International Society of Sports Nutrition")
                SourceItem("National Institutes of Health")
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        // Continue button
        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = "Continue",
                fontFamily = Inter,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
        }
    }
}

/**
 * Animated macro card with ring progress indicator
 */
@Composable
private fun AnimatedMacroCard(
    title: String,
    value: String,
    emoji: String,
    color: Color,
    progress: Float,
    startAnimation: Boolean,
    animationDelay: Int,
    modifier: Modifier = Modifier
) {
    // Animated progress value
    val animatedProgress = remember { Animatable(0f) }
    
    LaunchedEffect(startAnimation) {
        if (startAnimation) {
            delay(animationDelay.toLong())
            animatedProgress.animateTo(
                targetValue = progress,
                animationSpec = tween(
                    durationMillis = 1200,
                    easing = FastOutSlowInEasing
                )
            )
        }
    }
    
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier.height(140.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = emoji, fontSize = 16.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    fontFamily = Inter,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom
            ) {
                // Animated circular progress
                Box(
                    modifier = Modifier.size(60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(60.dp)) {
                        // Background arc
                        drawArc(
                            color = Color(0xFFE5E5E5),
                            startAngle = 135f,
                            sweepAngle = 270f,
                            useCenter = false,
                            style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                        )
                        // Animated progress arc
                        drawArc(
                            color = color,
                            startAngle = 135f,
                            sweepAngle = 270f * animatedProgress.value,
                            useCenter = false,
                            style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    Text(
                        text = value,
                        fontFamily = Inter,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Edit icon
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun GoalTip(emoji: String, text: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = emoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                fontFamily = Inter,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SourceItem(text: String) {
    Text(
        text = "• $text",
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(vertical = 2.dp)
    )
}

private fun calculateTargetDate(): String {
    val targetDate = LocalDate.now().plusMonths(3)
    return targetDate.format(DateTimeFormatter.ofPattern("dd MMM"))
}

/**
 * Calculate macro progress based on contribution to total calories.
 * Shows how balanced the macro distribution is.
 */
private fun calculateMacroProgress(macroGrams: Int, totalCalories: Int, macroType: String): Float {
    if (totalCalories <= 0) return 0.5f
    
    // Calculate calories from macro
    val caloriesFromMacro = when (macroType) {
        "carbs" -> macroGrams * 4
        "protein" -> macroGrams * 4
        "fats" -> macroGrams * 9
        else -> 0
    }
    
    // Calculate percentage of total calories
    val percentage = caloriesFromMacro.toFloat() / totalCalories
    
    // Ideal ranges for each macro
    val (minIdeal, maxIdeal) = when (macroType) {
        "carbs" -> 0.40f to 0.55f // 40-55% of calories
        "protein" -> 0.20f to 0.35f // 20-35% of calories
        "fats" -> 0.20f to 0.35f // 20-35% of calories
        else -> 0.25f to 0.35f
    }
    
    // Calculate progress - higher if within ideal range
    return when {
        percentage in minIdeal..maxIdeal -> 0.9f + (0.1f * (percentage - minIdeal) / (maxIdeal - minIdeal))
        percentage < minIdeal -> 0.3f + (0.6f * percentage / minIdeal)
        else -> 0.7f // Above ideal range
    }.coerceIn(0.2f, 1f)
}

/**
 * Calculate health score based on macro distribution and goal.
 * Returns score from 1-10.
 */
private fun calculateHealthScore(
    calories: Int,
    carbs: Int,
    protein: Int,
    fats: Int,
    goal: String
): Int {
    if (calories <= 0) return 7
    
    // Calculate macro percentages
    val carbsCals = carbs * 4
    val proteinCals = protein * 4
    val fatsCals = fats * 9
    val totalMacroCals = carbsCals + proteinCals + fatsCals
    
    if (totalMacroCals <= 0) return 7
    
    val carbsPercent = carbsCals.toFloat() / totalMacroCals
    val proteinPercent = proteinCals.toFloat() / totalMacroCals
    val fatsPercent = fatsCals.toFloat() / totalMacroCals
    
    var score = 5f
    
    // Check carbs balance (ideal: 40-55%)
    if (carbsPercent in 0.35f..0.60f) score += 1.5f
    else if (carbsPercent in 0.25f..0.65f) score += 0.5f
    
    // Check protein balance (ideal: 20-35%)
    if (proteinPercent in 0.15f..0.40f) score += 1.5f
    else if (proteinPercent in 0.10f..0.45f) score += 0.5f
    
    // Check fats balance (ideal: 20-35%)
    if (fatsPercent in 0.15f..0.40f) score += 1.5f
    else if (fatsPercent in 0.10f..0.45f) score += 0.5f
    
    // Bonus for appropriate calorie level
    when (goal) {
        "Lose Weight" -> if (calories in 1200..1800) score += 0.5f
        "Gain Weight" -> if (calories in 2200..3500) score += 0.5f
        else -> if (calories in 1800..2500) score += 0.5f
    }
    
    return score.coerceIn(1f, 10f).toInt()
}

