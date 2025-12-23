package com.example.calview.feature.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calview.core.ui.components.CalAICard
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.Restaurant

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onScroll: (() -> Unit)? = null
) {
    val state by viewModel.dashboardState.collectAsState()
    
    DashboardContent(
        state = state,
        onScroll = onScroll
    )
}

@Composable
fun DashboardContent(
    state: DashboardState,
    onScroll: (() -> Unit)? = null
) {
    val pagerState = rememberPagerState(pageCount = { 3 })

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            HeaderSection()
        }
        
        item {
            DateSelector()
        }

        item {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth().height(260.dp),
                pageSpacing = 16.dp
            ) { page ->
                when (page) {
                    0 -> Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        CaloriesCard(state.remainingCalories)
                        com.example.calview.feature.dashboard.components.MacroStatsRow(
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
                        WaterTrackerCard(consumed = 0)
                    }
                }
            }
            
            // Pager Indicators
            Row(
                Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(pagerState.pageCount) { iteration ->
                    val color = if (pagerState.currentPage == iteration) Color.Black else Color.LightGray
                    Box(
                        modifier = Modifier
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(color)
                            .size(6.dp)
                    )
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
                        Icon(com.example.calview.feature.dashboard.RecentMealIcon, null, modifier = Modifier.size(60.dp), tint = Color(0xFFE8F5E9))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Tap + to add your first meal of the day", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// imports moved to top

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    DashboardContent(
        state = DashboardState(
            remainingCalories = 1705,
            proteinG = 117,
            carbsG = 203,
            fatsG = 47,
            meals = emptyList()
        )
    )
}

@Composable
fun MicroStatsRow(fiber: Int, sugar: Int, sodium: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MicroCard(label = "Fiber left", value = "${fiber}g", icon = Icons.Default.Circle, iconTint = Color(0xFFA5D6A7), modifier = Modifier.weight(1f))
        MicroCard(label = "Sugar left", value = "${sugar}g", icon = Icons.Default.Circle, iconTint = Color(0xFFF48FB1), modifier = Modifier.weight(1f))
        MicroCard(label = "Sodium left", value = "${sodium}mg", icon = Icons.Default.Circle, iconTint = Color(0xFFFFCC80), modifier = Modifier.weight(1f))
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
                CircularProgressIndicator(progress = 0f, strokeWidth = 2.dp, color = iconTint, trackColor = Color(0xFFF3F3F3))
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
                progress = score / 10f,
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color = Color(0xFFE8F5E9),
                trackColor = Color(0xFFF3F3F3)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Carbs and fat are on track. You're low in calories and protein, which can slow weight loss and impact muscle retention.",
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
                Icon(Icons.Default.HealthAndSafety, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(24.dp))
                Text("Connect Google Health to track your steps", fontSize = 10.sp, textAlign = TextAlign.Center, color = Color.Gray)
            }
        }
        
        ActivityCard(
            title = "$burned",
            subtitle = "Calories burned",
            modifier = Modifier.weight(1f)
        ) {
             Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 16.dp)) {
                 Icon(Icons.Default.DirectionsWalk, null, modifier = Modifier.size(16.dp))
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
fun WaterTrackerCard(consumed: Int) {
    CalAICard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.WaterDrop, null, tint = Color(0xFF2196F3), modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Water", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("$consumed fl oz (0 cups)", fontSize = 14.sp, color = Color.Gray)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { }) { Icon(Icons.Default.RemoveCircleOutline, null) }
                IconButton(onClick = { }, modifier = Modifier.background(Color.Black, CircleShape).size(32.dp)) { 
                    Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(16.dp)) 
                }
            }
        }
    }
}

val RecentMealIcon: ImageVector
    @Composable
    get() = Icons.Default.Restaurant // Fallback

// imports moved to top

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
            Icon(
                Icons.Default.Favorite, 
                contentDescription = null, 
                modifier = Modifier.size(24.dp),
                tint = Color.Black
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Cal AI",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
        
        Surface(
            color = Color(0xFFF9F9F9),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.width(72.dp).height(40.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.LocalFireDepartment, null, tint = Color(0xFFE5A87B), modifier = Modifier.size(20.dp))
                Text(" 0", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DateSelector() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        listOf("Wed", "Thu", "Fri", "Sat", "Sun", "Mon", "Tue").forEachIndexed { index, day ->
            val isSelected = day == "Mon"
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = if (isSelected) {
                    Modifier
                        .background(Color(0xFF1C1C1E), RoundedCornerShape(24.dp))
                        .padding(vertical = 8.dp, horizontal = 12.dp)
                } else {
                    Modifier.padding(vertical = 8.dp, horizontal = 12.dp)
                }
            ) {
                Text(
                    day, 
                    fontSize = 12.sp, 
                    color = if (isSelected) Color.White else Color.Gray
                )
                Text(
                    (17 + index).toString(), 
                    fontWeight = FontWeight.Bold, 
                    color = if (isSelected) Color.White else Color.Black
                )
            }
        }
    }
}

@Composable
fun CaloriesCard(remaining: Int) {
    CalAICard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
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
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
            }
            
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(100.dp)) {
                com.example.calview.feature.dashboard.components.CalorieRing(
                    consumed = 0f, 
                    total = 1705f,
                    color = Color.Black,
                    strokeWidth = 10.dp
                )
                Icon(Icons.Default.LocalFireDepartment, null, tint = Color.Black, modifier = Modifier.size(24.dp))
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
fun RecentMealCard(meal: com.example.calview.core.data.local.MealEntity) {
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
                        imageVector = Icons.Default.LocalFireDepartment, 
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
                    MacroInfo(text = "${meal.protein}g", icon = Icons.Default.Favorite, Color(0xFFFF5252))
                    MacroInfo(text = "${meal.carbs}g", icon = Icons.Default.Eco, Color(0xFFFFAB40))
                    MacroInfo(text = "${meal.fats}g", icon = Icons.Default.Opacity, Color(0xFF448AFF))
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
