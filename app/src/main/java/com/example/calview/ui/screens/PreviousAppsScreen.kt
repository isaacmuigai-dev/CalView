package com.example.calview.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.example.calview.ui.screens.components.OnboardingTemplate
import com.example.calview.ui.theme.CalViewTheme
import com.example.calview.ui.components.CalAICard
import com.example.calview.ui.viewmodels.OnboardingViewModel

@Composable
fun PreviousAppsScreen(
    viewModel: OnboardingViewModel,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    OnboardingTemplate(
        title = "Have you tried other calorie tracking apps?",
        progress = 0.9f,
        onBack = onBack,
        onContinue = onContinue,
        canContinue = uiState.triedOtherApps != null
    ) {
        val options = listOf("Yes", "No")
        options.forEach { option ->
            CalAICard(
                title = option,
                isSelected = if (option == "Yes") uiState.triedOtherApps == true else uiState.triedOtherApps == false,
                onClick = { viewModel.onTriedOtherApps(option == "Yes") },
                leadingContent = {
                    Icon(
                        if (option == "Yes") Icons.Default.ThumbUp else Icons.Default.ThumbDown,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviousAppsScreenPreview() {
    CalViewTheme {
        // Note: This preview requires a ViewModel. In a real preview, you'd need to provide a mock ViewModel.
    }
}
