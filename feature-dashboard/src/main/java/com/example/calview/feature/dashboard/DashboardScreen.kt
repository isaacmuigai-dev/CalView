package com.example.calview.feature.dashboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calview.core.data.local.MealEntity
import com.example.calview.core.ui.components.CalAICard
import com.example.calview.feature.dashboard.components.CalorieRing
import com.example.calview.feature.dashboard.components.MacroStatsRow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel
) {
    val state by viewModel.dashboardState.collectAsState()

    DashboardContent(
        state = state,
        onDateSelected = { viewModel.selectDate(it) },
        onAddWater = { viewModel.addWater() },
        onRemoveWater = { viewModel.removeWater() }
    )
}

@Composable
fun DashboardContent(
    state: DashboardState,
    onDateSelected: (Calendar) -> Unit,
    onAddWater: () -> Unit,
    onRemoveWater: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            HeaderSection()
        }

        item {
            DateSelector(
                selectedDate = state.selectedDate, 
                onDateSelected = onDateSelected,
                mealsForDates = state.meals
            )
        }

        item {
            val pagerState = rememberPagerState(pageCount = { 3 })
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth().height(260.dp),
                pageSpacing = 16.dp
            ) { page ->
                when (page) {
                    0 -> Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        CaloriesCard(state.remainingCalories, state.consumedCalories, state.goalCalories)
                        MacroStatsRow(
                            protein = state.proteinG,
                            carbs = state.carbsG,
                            fats = state.fatsG
                        )
                    }
                    1 -> Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        MicroStatsRow(fiber = 38, sugar = 64, sodium = 2300)
                        HealthScoreCard(score = 0)
                    }
                    2 -> Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        ActivityRow(steps = 0, burned = 0)
                        WaterTrackerCard(consumed = state.waterConsumed, onAdd = onAddWater, onRemove = onRemoveWater)
                    }
                }
            }
        }

        item {
            Text(
                "Recently uploaded",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
            )
        }

        items(state.meals) { meal ->
            RecentMealCard(meal)
        }

        item {
            // Empty State if no meals
            if (state.meals.isEmpty()) {
                CalAICard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(RecentMealIcon, null, modifier = Modifier.size(60.dp), tint = Color(0xFFE8F5E9))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Tap + to add your first meal of the day", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    DashboardContent(
        state = DashboardState(),
        onDateSelected = {},
        onAddWater = {},
        onRemoveWater = {}
    )
}

@Composable
fun MicroStatsRow(fiber: Int, sugar: Int, sodium: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MicroCard(label = "Fiber left", value = "${fiber}g", icon = Icons.Filled.Circle, iconTint = Color(0xFFA5D6A7), modifier = Modifier.weight(1f))
        MicroCard(label = "Sugar left", value = "${sugar}g", icon = Icons.Filled.Circle, iconTint = Color(0xFFF48FB1), modifier = Modifier.weight(1f))
        MicroCard(label = "Sodium left", value = "${sodium}mg", icon = Icons.Filled.Circle, iconTint = Color(0xFFFFCC80), modifier = Modifier.weight(1f))
    }
}

@Composable
fun MicroCard(label: String, value: String, icon: ImageVector, iconTint: Color, modifier: Modifier = Modifier) {
    CalAICard(modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(label, fontSize = 12.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(12.dp))
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(40.dp).align(Alignment.CenterHorizontally)) {
                CircularProgressIndicator(progress = { 0f }, strokeWidth = 2.dp, color = iconTint, trackColor = Color(0xFFF3F3F3))
                Icon(icon, null, modifier = Modifier.size(12.dp), tint = iconTint.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
fun HealthScoreCard(score: Int) {
    CalAICard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Health score", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("$score/10", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { score / 10f },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(androidx.compose.foundation.shape.CircleShape),
                color = Color(0xFFE8F5E9),
                trackColor = Color(0xFFF3F3F3)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Carbs and fat are on track. You\'re low in calories and protein, which can slow weight loss and impact muscle retention.",
                fontSize = 14.sp,
                color = Color.Gray,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun ActivityRow(steps: Int, burned: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ActivityCard(
            title = "$steps /10000",
            subtitle = "Steps Today",
            modifier = Modifier.weight(1f)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(8.dp)) {
                Icon(Icons.Filled.HealthAndSafety, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(24.dp))
                Text("Connect Google Health to track your steps", fontSize = 10.sp, textAlign = TextAlign.Center, color = Color.Gray)
            }
        }
        
        ActivityCard(
            title = "$burned",
            subtitle = "Calories burned",
            modifier = Modifier.weight(1f)
        ) {
             Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 16.dp)) {
                 Icon(Icons.AutoMirrored.Filled.DirectionsWalk, null, modifier = Modifier.size(16.dp))
                 Text(" Steps +0", fontSize = 12.sp, fontWeight = FontWeight.Bold)
             }
        }
    }
}

@Composable
fun ActivityCard(title: String, subtitle: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    CalAICard(modifier = modifier.height(180.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, fontSize = 12.sp, color = Color.Gray)
            Spacer(modifier = Modifier.weight(1f))
            content()
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun WaterTrackerCard(consumed: Int, onAdd: () -> Unit, onRemove: () -> Unit) {
    CalAICard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.WaterDrop, null, tint = Color(0xFF2196F3), modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Water", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("$consumed fl oz (${consumed/8} cups)", fontSize = 14.sp, color = Color.Gray)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onRemove) { Icon(Icons.Filled.RemoveCircleOutline, null) }
                IconButton(onClick = onAdd, modifier = Modifier.background(Color.Black, androidx.compose.foundation.shape.CircleShape).size(32.dp)) { 
                    Icon(Icons.Filled.Add, null, tint = Color.White, modifier = Modifier.size(16.dp)) 
                }
            }
        }
    }
}

val RecentMealIcon: ImageVector
    @Composable
    get() = Icons.Filled.Restaurant // Fallback

@Composable
fun HeaderSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Camera icon with gradient background
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.linearGradient(
                            colors = listOf(Color(0xFF667EEA), Color(0xFF764BA2))
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.CameraAlt, 
                    contentDescription = "Camera", 
                    modifier = Modifier.size(22.dp),
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    "CalViewAI",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1C1C1E)
                )
                Text(
                    "Track & Thrive",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
        
        // Streak counter with modern pill design
        Surface(
            color = Color(0xFFFFF3E0),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.height(40.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.LocalFireDepartment, 
                    null, 
                    tint = Color(0xFFFF6B35), 
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "0", 
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF6B35)
                )
            }
        }
    }
}

@Composable
fun DateSelector(
    selectedDate: Calendar, 
    onDateSelected: (Calendar) -> Unit,
    mealsForDates: List<MealEntity> = emptyList()
) {
    val today = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    val listState = rememberLazyListState()
    
    // Generate 30 days: 15 before today, today, 14 after
    val days = (-15..14).map { offset ->
        Calendar.getInstance().apply { add(Calendar.DATE, offset) }
    }
    
    // Center on today (index 15) when first composed
    LaunchedEffect(Unit) {
        listState.scrollToItem(12) // Scroll to show today in view
    }
    
    // Helper to check if a date has meals logged
    fun hasMealsOnDate(date: Calendar): Boolean {
        // For now, just check if we have any meals (would need date field in MealEntity for real implementation)
        return mealsForDates.isNotEmpty() && date.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
    }
    
    Column(modifier = Modifier.fillMaxWidth()) {
        // Month and Year Header (centered, no navigation arrows)
        Text(
            text = dateFormat.format(selectedDate.time),
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1C1C1E),
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        // Scrollable Date Row with light gray background - touch scrolling only
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF0F0F0), RoundedCornerShape(16.dp))
                .padding(vertical = 8.dp)
        ) {
            LazyRow(
                state = listState,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(days.size) { index ->
                    val day = days[index]
                    val isToday = day.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) && 
                                  day.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                    val isSelected = day.get(Calendar.DAY_OF_YEAR) == selectedDate.get(Calendar.DAY_OF_YEAR) && 
                                     day.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR)
                    val hasMeals = hasMealsOnDate(day)
                    
                    DateItem(
                        day = day,
                        isToday = isToday,
                        isSelected = isSelected,
                        hasMeals = hasMeals,
                        onClick = { onDateSelected(day) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DateItem(
    day: Calendar,
    isToday: Boolean,
    isSelected: Boolean,
    hasMeals: Boolean,
    onClick: () -> Unit
) {
    // Colors matching reference image
    val coralColor = Color(0xFFE57373) // Coral/salmon for dates with logs
    val grayColor = Color(0xFFBDBDBD) // Gray for dashed circles
    val selectedBgColor = Color(0xFF1C1C1E) // Dark background for selected date
    
    // Calculate width to fit 7 items on screen (~44.dp per item)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(44.dp)
            .clickable { onClick() }
            .padding(vertical = 6.dp)
    ) {
        // Day name (Sun, Mon, etc.)
        Text(
            text = day.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault()) ?: "",
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) Color(0xFF1C1C1E) else Color(0xFF757575)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Date number with circular border - reduced size
        Box(
            modifier = Modifier
                .size(36.dp)
                .then(
                    when {
                        isSelected -> {
                            // Selected: dark filled circle background
                            Modifier.background(selectedBgColor, CircleShape)
                        }
                        hasMeals -> {
                            // Has meals: solid coral circle border
                            Modifier.drawBehind {
                                drawCircle(
                                    color = coralColor,
                                    style = Stroke(width = 2.dp.toPx())
                                )
                            }
                        }
                        else -> {
                            // No meals: dashed gray circle border
                            Modifier.drawBehind {
                                drawCircle(
                                    color = grayColor,
                                    style = Stroke(
                                        width = 1.5.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(
                                            floatArrayOf(5f, 5f), 
                                            0f
                                        )
                                    )
                                )
                            }
                        }
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = day.get(Calendar.DAY_OF_MONTH).toString(),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = when {
                    isSelected -> Color.White // White text on dark background
                    hasMeals -> coralColor
                    else -> Color(0xFF424242)
                }
            )
        }
    }
}

@Composable
fun CaloriesCard(remaining: Int, consumed: Int, goal: Int) {
    // State to toggle between "eaten" and "left" views
    var showEaten by remember { mutableStateOf(true) }
    
    // Animation for scale effect on tap
    val scale by animateFloatAsState(
        targetValue = if (showEaten) 1f else 1f,
        animationSpec = tween(150),
        label = "scale"
    )
    
    CalAICard(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable { showEaten = !showEaten }
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Animated content transition between eaten/left
                AnimatedContent(
                    targetState = showEaten,
                    transitionSpec = {
                        (slideInVertically { height -> height } + fadeIn(animationSpec = tween(300)))
                            .togetherWith(slideOutVertically { height -> -height } + fadeOut(animationSpec = tween(300)))
                    },
                    label = "calories_animation"
                ) { isEaten ->
                    Column {
                        if (isEaten) {
                            // "Eaten" view: shows consumed/goal
                            Text(
                                text = buildAnnotatedString {
                                    withStyle(SpanStyle(
                                        fontSize = 48.sp,
                                        fontWeight = FontWeight.Bold
                                    )) {
                                        append(consumed.toString())
                                    }
                                    withStyle(SpanStyle(
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = Color.Gray
                                    )) {
                                        append(" /$goal")
                                    }
                                }
                            )
                            Text(
                                text = buildAnnotatedString {
                                    append("Calories ")
                                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                        append("eaten")
                                    }
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        } else {
                            // "Left" view: shows remaining only
                            Text(
                                text = remaining.toString(),
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 48.sp
                                )
                            )
                            Text(
                                text = buildAnnotatedString {
                                    append("Calories ")
                                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                        append("left")
                                    }
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
            
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(100.dp)) {
                CalorieRing(
                    consumed = consumed.toFloat(), 
                    goal = goal.toFloat(),
                )
                Icon(Icons.Filled.LocalFireDepartment, null, tint = Color.Black, modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Composable
fun MacroSummaryRow(state: DashboardState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MacroCard("Protein", "${state.proteinG}g", Modifier.weight(1f))
        MacroCard("Carbs", "${state.carbsG}g", Modifier.weight(1f))
        MacroCard("Fat", "${state.fatsG}g", Modifier.weight(1f))
    }
}

@Composable
fun MacroCard(label: String, value: String, modifier: Modifier = Modifier) {
    CalAICard(modifier = modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
    }
}

@Composable
fun RecentMealCard(meal: MealEntity) {
    CalAICard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Meal Image from screenshot
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        meal.name, 
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "2:10 PM", // Placeholder time
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.LocalFireDepartment, 
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "${meal.calories} calories", 
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MacroInfo(text = "${meal.protein}g", icon = Icons.Filled.Favorite, Color(0xFFFF5252))
                    MacroInfo(text = "${meal.carbs}g", icon = Icons.Filled.Eco, Color(0xFFFFAB40))
                    MacroInfo(text = "${meal.fats}g", icon = Icons.Filled.WaterDrop, Color(0xFF448AFF))
                }
            }
        }
    }
}

@Composable
fun MacroInfo(text: String, icon: ImageVector, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = color)
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }
}