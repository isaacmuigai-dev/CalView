package com.example.calview.feature.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.calview.feature.onboarding.components.OnboardingScreenLayout

/**
 * Birthdate input screen with Month, Day, Year wheel pickers.
 */
@Composable
fun BirthdateScreen(
    currentStep: Int = 6,
    totalSteps: Int = 7,
    selectedMonth: String = "January",
    selectedDay: Int = 1,
    selectedYear: Int = 2001,
    onMonthChanged: (String) -> Unit,
    onDayChanged: (Int) -> Unit,
    onYearChanged: (Int) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    val months = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    
    val days = (1..31).toList()
    val years = (1940..2010).toList()
    
    OnboardingScreenLayout(
        currentStep = currentStep,
        totalSteps = totalSteps,
        title = "When were you born?",
        subtitle = "This will be taken into account when calculating your daily nutrition goals.",
        onBack = onBack,
        onContinue = onContinue,
        continueEnabled = true
    ) {
        Spacer(modifier = Modifier.weight(0.3f))
        
        // Date picker area
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.7f),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Month picker
            WheelPicker(
                items = months,
                selectedIndex = months.indexOf(selectedMonth).coerceAtLeast(0),
                onSelectedIndexChange = { onMonthChanged(months[it]) },
                modifier = Modifier.weight(1f)
            )
            
            // Day picker
            WheelPicker(
                items = days.map { String.format("%02d", it) },
                selectedIndex = (selectedDay - 1).coerceIn(0, days.lastIndex),
                onSelectedIndexChange = { onDayChanged(days[it]) },
                modifier = Modifier.weight(0.6f)
            )
            
            // Year picker
            WheelPicker(
                items = years.map { it.toString() },
                selectedIndex = (selectedYear - 1940).coerceIn(0, years.lastIndex),
                onSelectedIndexChange = { onYearChanged(years[it]) },
                modifier = Modifier.weight(0.8f)
            )
        }
    }
}
