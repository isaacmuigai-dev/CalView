package com.example.calview.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.example.calview.ui.screens.components.OnboardingTemplate
import com.example.calview.ui.theme.CalViewTheme
import com.example.calview.ui.viewmodels.OnboardingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReferralCodeScreen(
    viewModel: OnboardingViewModel,
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    OnboardingTemplate(
        title = "Enter referral code\n(optional)",
        subtitle = "You can skip this step",
        progress = 0.98f, // Near the end
        onBack = onBack,
        onContinue = onContinue
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(Color(0xFFF9F9F9), RoundedCornerShape(32.dp))
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextField(
                    value = uiState.referralCode,
                    onValueChange = { viewModel.onReferralCodeChanged(it) },
                    placeholder = { Text("Referral Code", color = Color.LightGray) },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                )
                
                Button(
                    onClick = { /* Submit logic would go here */ },
                    modifier = Modifier.height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE0E0E0),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(24.dp),
                    enabled = uiState.referralCode.isNotEmpty()
                ) {
                    Text("Submit", fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReferralCodeScreenPreview() {
    CalViewTheme {
        // Note: This preview requires a ViewModel. In a real preview, you'd need to provide a mock ViewModel.
    }
}
