package com.example.calview.feature.trends

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calview.core.ui.components.CalAICard
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun ProgressScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ProgressPhotosCard()
        
        TimeframeTabs()

        TotalCaloriesCard()
        
        YourBMICard(bmi = 19.21f)

        GoalProgressCard()
        
        Spacer(modifier = Modifier.height(80.dp)) // Leave space for FAB/BottomBar
    }
}

@Composable
fun ProgressPhotosCard() {
    CalAICard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Progress Photos", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Photo Placeholder
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color(0xFFF3F3F3), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, null, tint = Color.LightGray, modifier = Modifier.size(40.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        "Want to add a photo to track your progress?",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Add, null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Text(" Upload a Photo", color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun TimeframeTabs() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        listOf("This Week", "Last Week", "2 wks. ago", "3 wks. ago").forEachIndexed { index, title ->
            val isSelected = index == 0
            Surface(
                color = if (isSelected) Color.White else Color.Transparent,
                shape = RoundedCornerShape(12.dp),
                border = if (isSelected) BorderStroke(1.dp, Color(0xFFE0E0E0)) else null,
                modifier = Modifier.height(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp)) {
                    Text(
                        title, 
                        fontSize = 14.sp, 
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.Black else Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun TotalCaloriesCard() {
    CalAICard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Total calories", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text("0.0", fontSize = 32.sp, fontWeight = FontWeight.Bold)
                Text(" cals", fontSize = 16.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 4.dp))
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Bar Chart Mockup
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                repeat(5) {
                    Divider(color = Color(0xFFF3F3F3), thickness = 1.dp)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                    Text(day, fontSize = 12.sp, color = Color.Gray)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MacroLegend(Color(0xFFD64D50), Icons.Default.Favorite, "Protein")
                Spacer(modifier = Modifier.width(16.dp))
                MacroLegend(Color(0xFFE5A87B), Icons.Default.Grass, "Carbs")
                Spacer(modifier = Modifier.width(16.dp))
                MacroLegend(Color(0xFF6A8FB3), Icons.Default.Opacity, "Fats")
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Surface(
                color = Color(0xFFE8F5E9),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Getting started is the hardest part. You're ready for this!",
                    modifier = Modifier.padding(12.dp),
                    color = Color(0xFF2E7D32),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun MacroLegend(color: Color, icon: ImageVector, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
        Text(" $label", fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun YourBMICard(bmi: Float) {
    CalAICard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Your BMI", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(bmi.toString(), fontSize = 32.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Your weight is ", fontSize = 14.sp, color = Color.Gray)
                Surface(
                    color = Color(0xFF4CAF50),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "Healthy", 
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Icon(Icons.Default.HelpOutline, null, tint = Color.LightGray, modifier = Modifier.size(20.dp))
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // BMI Gauge
            Box(modifier = Modifier.fillMaxWidth().height(12.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val colors = listOf(Color(0xFF2196F3), Color(0xFF4CAF50), Color(0xFFFFC107), Color(0xFFF44336))
                    val segmentWidth = size.width / colors.size
                    colors.forEachIndexed { index, color ->
                        drawRect(
                            color = color,
                            topLeft = Offset(index * segmentWidth, 0f),
                            size = Size(segmentWidth, size.height)
                        )
                    }
                    
                    // BMI Indicator
                    val indicatorPos = size.width * (bmi / 40f).coerceIn(0f, 1f)
                    drawRect(
                        color = Color(0xFF1B5E20),
                        topLeft = Offset(indicatorPos - 2.dp.toPx(), -4.dp.toPx()),
                        size = Size(4.dp.toPx(), size.height + 8.dp.toPx())
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                BMILabel(Color(0xFF2196F3), "Underweight")
                BMILabel(Color(0xFF4CAF50), "Healthy")
                BMILabel(Color(0xFFFFC107), "Overweight")
                BMILabel(Color(0xFFF44336), "Obese")
            }
        }
    }
}

@Composable
fun BMILabel(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
        Text(" $label", fontSize = 10.sp, color = Color.Gray)
    }
}

@Composable
fun GoalProgressCard() {
    CalAICard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Reusing existing goal progress logic with minor updates
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Goal Progress", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Surface(
                    color = Color(0xFFF3F3F3),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Flag, null, tint = Color.Black, modifier = Modifier.size(14.dp))
                        Text(" 0% of goal", fontSize = 12.sp)
                        Icon(Icons.Default.Edit, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Weight card contents
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("My Weight", color = Color.Gray, fontSize = 14.sp)
                    Text("119 lb", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
                Column(modifier = Modifier.weight(1f)) {
                   Text("Goal", color = Color.Gray, fontSize = 14.sp)
                   Text("119 lbs", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
