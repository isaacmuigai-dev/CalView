package com.example.calview.feature.trends

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calview.core.ui.components.CalAICard
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.StrokeCap

@Composable
fun ProgressScreen() {
    ProgressContent(
        currentWeight = 119f,
        goalWeight = 119f,
        dayStreak = 1,
        completedDays = listOf(false, false, true, true, true, false, false), // S M T W T F S
        totalCalories = 321.0f,
        caloriesChange = 100,
        weeklyMacros = listOf(
            DayMacros(0f, 0f, 0f),  // Sun
            DayMacros(0f, 0f, 0f),  // Mon
            DayMacros(0f, 0f, 0f),  // Tue
            DayMacros(30f, 35f, 5f), // Wed
            DayMacros(2f, 0f, 0f),  // Thu
            DayMacros(0f, 0f, 0f),  // Fri
            DayMacros(0f, 0f, 0f)   // Sat
        ),
        weightHistory = listOf(133.3f, 131f, 129.4f, 127f, 125.5f, 123f, 119f)
    )
}

data class DayMacros(val protein: Float, val carbs: Float, val fats: Float)

@Composable
fun ProgressContent(
    currentWeight: Float,
    goalWeight: Float,
    dayStreak: Int,
    completedDays: List<Boolean>,
    totalCalories: Float,
    caloriesChange: Int,
    weeklyMacros: List<DayMacros>,
    weightHistory: List<Float>
) {
    var selectedTimePeriod by remember { mutableIntStateOf(0) }
    var selectedWeekPeriod by remember { mutableIntStateOf(0) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        Text(
            text = "Progress",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Row 1: My Weight + Day Streak
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MyWeightCard(
                currentWeight = currentWeight,
                goalWeight = goalWeight,
                nextWeighIn = 2,
                modifier = Modifier.weight(1f)
            )
            DayStreakCard(
                streak = dayStreak,
                completedDays = completedDays,
                modifier = Modifier.weight(1f)
            )
        }
        
        // Time Period Tabs (90 Days, 6 Months, etc.)
        TimePeriodTabs(
            items = listOf("90 Days", "6 Months", "1 Year", "All time"),
            selectedIndex = selectedTimePeriod,
            onSelected = { selectedTimePeriod = it }
        )
        
        // Goal Progress Card with Line Chart
        GoalProgressCardWithChart(
            goalPercent = 0,
            weightHistory = weightHistory,
            startDate = "Dec 16",
            endDate = "Dec 22"
        )
        
        // Motivational Banner
        MotivationalBanner(
            message = "Getting started is the hardest part. You're ready for this!"
        )
        
        // Progress Photos Card
        ProgressPhotosCard()
        
        // Week Period Tabs
        TimePeriodTabs(
            items = listOf("This Week", "Last Week", "2 wks. ago", "3 wks. ago"),
            selectedIndex = selectedWeekPeriod,
            onSelected = { selectedWeekPeriod = it }
        )
        
        // Total Calories Card with Stacked Bar Chart
        TotalCaloriesCardWithChart(
            totalCalories = totalCalories,
            changePercent = caloriesChange,
            weeklyMacros = weeklyMacros
        )
        
        // Your BMI Card
        YourBMICard(
            bmi = 19.21f,
            weightKg = 54f,
            heightCm = 167.6f
        )
        
        // Final Motivational Banner
        MotivationalBanner(
            message = "It's the final stretch! Push yourself!"
        )
        
        Spacer(modifier = Modifier.height(80.dp))
    }
}

// ============ MY WEIGHT CARD ============
@Composable
fun MyWeightCard(
    currentWeight: Float,
    goalWeight: Float,
    nextWeighIn: Int,
    modifier: Modifier = Modifier
) {
    val progress = if (goalWeight > 0) (currentWeight / goalWeight).coerceIn(0f, 1f) else 0f
    
    CalAICard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "My Weight",
                fontSize = 12.sp,
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "${currentWeight.toInt()} lb",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(Color(0xFFE8E8E8), RoundedCornerShape(3.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .background(Color(0xFF4CAF50), RoundedCornerShape(3.dp))
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row {
                Text(
                    text = "Goal ",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = "${goalWeight.toInt()} lbs",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Next weigh-in footer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                    .padding(vertical = 8.dp, horizontal = 12.dp)
            ) {
                Text(
                    text = "Next weight-in: ${nextWeighIn}d",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

// ============ DAY STREAK CARD ============
@Composable
fun DayStreakCard(
    streak: Int,
    completedDays: List<Boolean>,
    modifier: Modifier = Modifier
) {
    val weekDays = listOf("S", "M", "T", "W", "T", "F", "S")
    
    CalAICard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Fire icon with streak number
            Box(
                modifier = Modifier.size(64.dp),
                contentAlignment = Alignment.Center
            ) {
                // Fire icon
                Icon(
                    imageVector = Icons.Filled.Whatshot,
                    contentDescription = "Streak",
                    modifier = Modifier.size(56.dp),
                    tint = Color(0xFFFF9800)
                )
                // Streak number inside flame
                Text(
                    text = streak.toString(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.offset(y = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "Day streak",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFF57C00)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Week day indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                weekDays.forEachIndexed { index, day ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = day,
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(
                                    if (completedDays.getOrElse(index) { false })
                                        Color(0xFF4CAF50)
                                    else
                                        Color(0xFFE0E0E0),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (completedDays.getOrElse(index) { false }) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(10.dp),
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============ TIME PERIOD TABS ============
@Composable
fun TimePeriodTabs(
    items: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEachIndexed { index, title ->
            val isSelected = index == selectedIndex
            Surface(
                color = if (isSelected) Color.White else Color.Transparent,
                shape = RoundedCornerShape(12.dp),
                border = if (isSelected) BorderStroke(1.dp, Color(0xFFE0E0E0)) else null,
                modifier = Modifier
                    .height(36.dp)
                    .clickable { onSelected(index) }
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.Black else Color.Gray
                    )
                }
            }
        }
    }
}

// ============ GOAL PROGRESS CARD WITH LINE CHART ============
@Composable
fun GoalProgressCardWithChart(
    goalPercent: Int,
    weightHistory: List<Float>,
    startDate: String,
    endDate: String
) {
    CalAICard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Goal Progress",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Surface(
                    color = Color(0xFFF5F5F5),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Flag,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color.Black
                        )
                        Text(
                            text = "$goalPercent%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "of goal",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            modifier = Modifier.size(14.dp),
                            tint = Color.Gray
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Line Chart
            if (weightHistory.isNotEmpty()) {
                val maxWeight = weightHistory.maxOrNull() ?: 140f
                val minWeight = weightHistory.minOrNull() ?: 110f
                val range = maxWeight - minWeight
                val yLabels = listOf(maxWeight, maxWeight - range * 0.25f, maxWeight - range * 0.5f, maxWeight - range * 0.75f, minWeight)
                
                Row(modifier = Modifier.height(180.dp)) {
                    // Y-axis labels
                    Column(
                        modifier = Modifier
                            .width(40.dp)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        yLabels.forEach { value ->
                            Text(
                                text = String.format("%.1f", value),
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }
                    }
                    
                    // Chart area
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val chartHeight = size.height
                            val chartWidth = size.width
                            val pointSpacing = chartWidth / (weightHistory.size - 1).coerceAtLeast(1)
                            
                            // Draw horizontal dashed lines
                            yLabels.forEachIndexed { index, _ ->
                                val y = chartHeight * index / (yLabels.size - 1)
                                drawLine(
                                    color = Color(0xFFE0E0E0),
                                    start = Offset(0f, y),
                                    end = Offset(chartWidth, y),
                                    strokeWidth = 1.dp.toPx()
                                )
                            }
                            
                            // Create path for area fill
                            val path = Path()
                            weightHistory.forEachIndexed { index, weight ->
                                val x = index * pointSpacing
                                val normalizedY = if (range > 0) (maxWeight - weight) / range else 0.5f
                                val y = normalizedY * chartHeight
                                
                                if (index == 0) {
                                    path.moveTo(x, y)
                                } else {
                                    path.lineTo(x, y)
                                }
                            }
                            
                            // Close path for area fill
                            val areaPath = Path()
                            areaPath.addPath(path)
                            areaPath.lineTo(chartWidth, chartHeight)
                            areaPath.lineTo(0f, chartHeight)
                            areaPath.close()
                            
                            // Draw area fill
                            drawPath(
                                path = areaPath,
                                color = Color(0xFFF0F0F0)
                            )
                            
                            // Draw line
                            drawPath(
                                path = path,
                                color = Color(0xFF424242),
                                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // X-axis labels
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 40.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = startDate, fontSize = 10.sp, color = Color.Gray)
                    Text(text = endDate, fontSize = 10.sp, color = Color.Gray)
                }
            }
        }
    }
}

// ============ MOTIVATIONAL BANNER ============
@Composable
fun MotivationalBanner(message: String) {
    Surface(
        color = Color(0xFFE8F5E9),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            color = Color(0xFF2E7D32),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}

// ============ PROGRESS PHOTOS CARD ============
@Composable
fun ProgressPhotosCard() {
    CalAICard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Progress Photos",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Person silhouette
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color(0xFFF3F3F3), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color(0xFFBDBDBD),
                        modifier = Modifier.size(36.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text(
                        text = "Want to add a photo to track your progress?",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        lineHeight = 18.sp
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedButton(
                        onClick = { /* TODO: Open photo picker */ },
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.Black
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Upload a Photo",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}

// ============ TOTAL CALORIES CARD WITH STACKED BAR CHART ============
@Composable
fun TotalCaloriesCardWithChart(
    totalCalories: Float,
    changePercent: Int,
    weeklyMacros: List<DayMacros>
) {
    val weekDays = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val maxTotal = weeklyMacros.maxOfOrNull { it.protein + it.carbs + it.fats } ?: 70f
    
    CalAICard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Text(
                text = "Total calories",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Calories value with change indicator
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = String.format("%.1f", totalCalories),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = " cals",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Filled.ArrowUpward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color(0xFF4CAF50)
                )
                Text(
                    text = "$changePercent%",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Stacked Bar Chart
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                weeklyMacros.forEachIndexed { index, macros ->
                    val total = macros.protein + macros.carbs + macros.fats
                    val heightFraction = if (maxTotal > 0) total / maxTotal else 0f
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Stacked bar
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .weight(1f),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            if (total > 0) {
                                Column(
                                    modifier = Modifier.height((140 * heightFraction).dp)
                                ) {
                                    // Protein (top - salmon)
                                    if (macros.protein > 0) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .weight(macros.protein / total)
                                                .background(
                                                    Color(0xFFE57373),
                                                    RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                                )
                                        )
                                    }
                                    // Carbs (middle - orange)
                                    if (macros.carbs > 0) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .weight(macros.carbs / total)
                                                .background(Color(0xFFFFB74D))
                                        )
                                    }
                                    // Fats (bottom - blue)
                                    if (macros.fats > 0) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .weight(macros.fats / total)
                                                .background(
                                                    Color(0xFF64B5F6),
                                                    RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp)
                                                )
                                        )
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Day label
                        Text(
                            text = weekDays[index],
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Horizontal dashed lines (Y-axis indicators)
            // Already drawn in chart
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MacroLegendItem(color = Color(0xFFE57373), icon = Icons.Filled.Favorite, label = "Protein")
                Spacer(modifier = Modifier.width(16.dp))
                MacroLegendItem(color = Color(0xFFFFB74D), icon = Icons.Filled.Grass, label = "Carbs")
                Spacer(modifier = Modifier.width(16.dp))
                MacroLegendItem(color = Color(0xFF64B5F6), icon = Icons.Filled.WaterDrop, label = "Fats")
            }
        }
    }
}

@Composable
fun MacroLegendItem(color: Color, icon: ImageVector, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = color
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// ============ YOUR BMI CARD ============
@Composable
fun YourBMICard(
    bmi: Float,
    weightKg: Float,
    heightCm: Float,
    modifier: Modifier = Modifier
) {
    // Calculate BMI if not provided
    val calculatedBmi = if (bmi > 0) bmi else {
        val heightM = heightCm / 100f
        if (heightM > 0) weightKg / (heightM * heightM) else 0f
    }
    
    // Determine health status
    val (statusText, statusColor) = when {
        calculatedBmi < 18.5f -> "Underweight" to Color(0xFF2196F3)
        calculatedBmi < 25f -> "Healthy" to Color(0xFF4CAF50)
        calculatedBmi < 30f -> "Overweight" to Color(0xFFFFC107)
        else -> "Obese" to Color(0xFFF44336)
    }
    
    CalAICard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Title
            Text(
                text = "Your BMI",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // BMI Value row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = String.format("%.2f", calculatedBmi),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "Your weight is",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.width(6.dp))
                
                // Health status badge
                Surface(
                    color = statusColor,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = statusText,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Help icon
                Icon(
                    imageVector = Icons.Filled.HelpOutline,
                    contentDescription = "BMI Info",
                    tint = Color.LightGray,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // BMI Gradient Bar with indicator
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val barHeight = size.height
                    val barWidth = size.width
                    
                    // Draw gradient segments (Underweight, Healthy, Overweight, Obese)
                    // BMI ranges: <18.5 (Under), 18.5-25 (Healthy), 25-30 (Over), >30 (Obese)
                    val segments = listOf(
                        18.5f to Color(0xFF2196F3),  // Underweight - Blue
                        25f to Color(0xFF4CAF50),    // Healthy - Green
                        30f to Color(0xFFFFC107),    // Overweight - Yellow
                        40f to Color(0xFFF44336)     // Obese - Red
                    )
                    
                    val totalRange = 40f - 15f // 15 to 40 BMI range for display
                    var currentX = 0f
                    var prevBmi = 15f
                    
                    segments.forEach { (maxBmi, color) ->
                        val segmentWidth = ((maxBmi - prevBmi) / totalRange) * barWidth
                        drawRect(
                            color = color,
                            topLeft = Offset(currentX, 0f),
                            size = Size(segmentWidth, barHeight)
                        )
                        currentX += segmentWidth
                        prevBmi = maxBmi
                    }
                    
                    // Draw position indicator (black rectangle)
                    val indicatorPos = ((calculatedBmi - 15f) / totalRange).coerceIn(0f, 1f) * barWidth
                    val indicatorWidth = 4.dp.toPx()
                    val indicatorHeight = barHeight + 8.dp.toPx()
                    
                    drawRect(
                        color = Color(0xFF1C1C1E),
                        topLeft = Offset(indicatorPos - indicatorWidth / 2, -4.dp.toPx()),
                        size = Size(indicatorWidth, indicatorHeight)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                BMILegendItem(color = Color(0xFF2196F3), label = "Underweight")
                BMILegendItem(color = Color(0xFF4CAF50), label = "Healthy")
                BMILegendItem(color = Color(0xFFFFC107), label = "Overweight")
                BMILegendItem(color = Color(0xFFF44336), label = "Obese")
            }
        }
    }
}

@Composable
fun BMILegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color.Gray
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProgressScreenPreview() {
    ProgressScreen()
}
