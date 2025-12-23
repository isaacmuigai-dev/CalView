package com.example.calview.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.example.calview.ui.screens.components.OnboardingTemplate

@Composable
fun CreateAccountScreen(
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    OnboardingTemplate(
        title = "Create an account",
        progress = 0.995f,
        onBack = onBack,
        showBottomBar = false // Custom buttons here
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C1C1E)),
                shape = RoundedCornerShape(32.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Google Icon Placeholder
                    Text(
                        "G ", 
                        color = Color.Red, 
                        fontWeight = FontWeight.Black, 
                        fontSize = 20.sp,
                        fontFamily = FontFamily.SansSerif
                    )
                    Text(
                        "Sign in with Google",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontFamily = FontFamily.SansSerif
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedButton(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp),
                shape = RoundedCornerShape(32.dp)
            ) {
                Text(
                    "Skip",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    fontFamily = FontFamily.SansSerif
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreateAccountScreenPreview() {
    CreateAccountScreen(
        onContinue = {},
        onBack = {}
    )
}
