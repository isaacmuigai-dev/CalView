package com.example.calview.feature.trends

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calview.core.ui.theme.Inter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun GoalJourneyCard(
    goal: String,
    currentWeight: Float,
    targetWeight: Float,
    weeklyPace: Float,
    weeksToGoal: Int,
    weightDiff: Int,
    estimatedGoalDate: LocalDate,
    modifier: Modifier = Modifier
) {
    val actionWord = when (goal) {
        "Lose Weight" -> "Lose"
        "Gain Weight" -> "Gain"
        else -> "Maintain"
    }

    val targetDateStr = estimatedGoalDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Your Goal Journey",
                fontFamily = Inter,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(20.dp))

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
                        fontSize = 22.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Arrow
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "→",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

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
                        fontSize = 22.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Details Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Weekly Pace
                JourneyStatItem(
                    label = "⚡ Weekly Pace",
                    value = "${"%.1f".format(weeklyPace)} kg",
                    modifier = Modifier.weight(1f)
                )

                // To ${actionWord}
                JourneyStatItem(
                    label = "📊 To $actionWord",
                    value = "$weightDiff kg",
                    modifier = Modifier.weight(1f)
                )

                // Time Estimate
                JourneyStatItem(
                    label = "⏱️ Estimated",
                    value = "$weeksToGoal weeks",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Target Date
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
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
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = targetDateStr,
                        fontFamily = Inter,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun JourneyStatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontFamily = Inter,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontFamily = Inter,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
