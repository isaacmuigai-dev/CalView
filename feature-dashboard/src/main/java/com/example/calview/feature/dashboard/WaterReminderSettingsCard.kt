package com.example.calview.feature.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calview.core.ui.components.PremiumBadge

/**
 * Water reminder settings card for premium users.
 * Allows configuration of hydration reminders.
 */
@Composable
fun WaterReminderSettingsCard(
    isPremium: Boolean,
    enabled: Boolean,
    intervalHours: Int,
    startHour: Int,
    endHour: Int,
    dailyGoalMl: Int,
    onEnabledChange: (Boolean) -> Unit,
    onIntervalChange: (Int) -> Unit,
    onStartHourChange: (Int) -> Unit,
    onEndHourChange: (Int) -> Unit,
    onDailyGoalChange: (Int) -> Unit,
    onUpgradeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .semantics {
                    contentDescription = if (isPremium) {
                        "Water Reminders Settings. " +
                        if (enabled) "Enabled. Reminding every $intervalHours hours. Daily goal: ${dailyGoalMl}ml."
                        else "Disabled. Tap the switch to enable."
                    } else {
                        "Water Reminders. Premium feature. Tap to upgrade."
                    }
                }
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = "Water Reminders",
                        tint = Color(0xFF2196F3),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Water Reminders",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                if (!isPremium) {
                    PremiumBadge()
                } else {
                    Switch(
                        checked = enabled,
                        onCheckedChange = onEnabledChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF2196F3)
                        ),
                        modifier = Modifier.semantics {
                            role = Role.Switch
                            stateDescription = if (enabled) "Enabled" else "Disabled"
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (isPremium) {
                // Reminder settings (shown when premium)
                if (enabled) {
                    // Interval slider
                    Column {
                        Text(
                            text = "Remind every $intervalHours hours",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Slider(
                            value = intervalHours.toFloat(),
                            onValueChange = { onIntervalChange(it.toInt()) },
                            valueRange = 1f..6f,
                            steps = 4,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF2196F3),
                                activeTrackColor = Color(0xFF2196F3)
                            ),
                            modifier = Modifier.semantics {
                                contentDescription = "Reminder interval: $intervalHours hours. Drag to adjust from 1 to 6 hours."
                            }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Active hours
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Active hours",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${formatHour(startHour)} - ${formatHour(endHour)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF2196F3)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Daily goal
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Daily goal",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { onDailyGoalChange((dailyGoalMl - 240).coerceAtLeast(500)) } // ~8 oz decrement
                            ) {
                                Icon(Icons.Default.Remove, "Decrease", tint = Color(0xFF2196F3))
                            }
                            // Convert ml to fl oz for display (1 ml = 0.033814 fl oz)
                            val dailyGoalFlOz = (dailyGoalMl * 0.033814).toInt()
                            Text(
                                text = "${dailyGoalFlOz} fl oz",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            IconButton(
                                onClick = { onDailyGoalChange((dailyGoalMl + 240).coerceAtMost(5000)) } // ~8 oz increment
                            ) {
                                Icon(Icons.Default.Add, "Increase", tint = Color(0xFF2196F3))
                            }
                        }
                    }
                } else {
                    Text(
                        text = "Enable to receive periodic hydration reminders throughout the day.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Non-premium view
                Text(
                    text = "Get periodic reminders to stay hydrated! Premium members can customize reminder intervals and goals.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onUpgradeClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFD700)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Unlock Water Reminders",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun formatHour(hour: Int): String {
    val amPm = if (hour < 12) "AM" else "PM"
    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return "$displayHour $amPm"
}
