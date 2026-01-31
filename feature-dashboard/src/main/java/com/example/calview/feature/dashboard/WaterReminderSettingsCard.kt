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
import androidx.compose.ui.res.stringResource


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
    servingSize: Int, // ml
    onEnabledChange: (Boolean) -> Unit,
    onIntervalChange: (Int) -> Unit,
    onStartHourChange: (Int) -> Unit,
    onEndHourChange: (Int) -> Unit,
    onDailyGoalChange: (Int) -> Unit,
    onServingSizeChange: (Int) -> Unit,
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
        val enabledDesc = stringResource(R.string.water_reminders_enabled_desc, intervalHours, dailyGoalMl)
        val disabledDesc = stringResource(R.string.water_reminders_disabled_desc)
        val premiumDesc = stringResource(R.string.water_reminders_premium_desc)

        Column(
            modifier = Modifier
                .padding(16.dp)
                .semantics {
                    contentDescription = if (isPremium) {
                        if (enabled) enabledDesc else disabledDesc
                    } else {
                        premiumDesc
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
                        contentDescription = stringResource(R.string.water_reminders_title),
                        tint = Color(0xFF2196F3),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.water_reminders_title),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                if (!isPremium) {
                    PremiumBadge()
                } else {
                    val stateEnabled = stringResource(R.string.state_enabled)
                    val stateDisabled = stringResource(R.string.state_disabled)
                    Switch(
                        checked = enabled,
                        onCheckedChange = onEnabledChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF2196F3)
                        ),
                        modifier = Modifier.semantics {
                            role = Role.Switch
                            stateDescription = if (enabled) stateEnabled else stateDisabled
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
                            text = stringResource(R.string.remind_every_hours, intervalHours),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val sliderDesc = stringResource(R.string.reminder_interval_desc, intervalHours)
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
                                contentDescription = sliderDesc
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
                            text = stringResource(R.string.active_hours_label),
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
                            text = stringResource(R.string.daily_goal_label),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { onDailyGoalChange((dailyGoalMl - 250).coerceAtLeast(500)) } 
                            ) {
                                Icon(Icons.Default.Remove, stringResource(R.string.decrease_desc), tint = Color(0xFF2196F3))
                            }
                            Text(
                                text = stringResource(R.string.ml_suffix, dailyGoalMl),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            IconButton(
                                onClick = { onDailyGoalChange((dailyGoalMl + 250).coerceAtMost(5000)) }
                            ) {
                                Icon(Icons.Default.Add, stringResource(R.string.increase_desc), tint = Color(0xFF2196F3))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Serving Size Selection
                    Column {
                        Text(
                            text = stringResource(R.string.default_serving_size_label),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(250, 500, 750, 1000).forEach { size ->
                                val selected = servingSize == size
                                FilterChip(
                                    selected = selected,
                                    onClick = { onServingSizeChange(size) },
                                    label = { Text(stringResource(R.string.ml_suffix, size)) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF2196F3),
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = stringResource(R.string.enable_reminders_hint),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Non-premium view
                Text(
                    text = stringResource(R.string.premium_upgrade_hint_water),
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
                        stringResource(R.string.unlock_water_reminders),
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
