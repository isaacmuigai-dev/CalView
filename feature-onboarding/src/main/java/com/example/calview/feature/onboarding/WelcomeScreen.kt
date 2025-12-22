package com.example.calview.feature.onboarding

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import com.example.calview.core.ui.components.CalAIButton
import com.example.calview.core.ui.theme.Inter

@Composable
fun WelcomeScreen(
    onGetStarted: () -> Unit,
    onSignIn: () -> Unit
) {
    Surface(
        color = Color.White,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(
                text = "Calorie tracking\nmade easy",
                fontFamily = Inter,
                fontWeight = FontWeight.Bold,
                fontSize = 44.sp,
                lineHeight = 52.sp,
                textAlign = TextAlign.Center,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 64.dp)
            )

            CalAIButton(
                text = "Get Started",
                onClick = onGetStarted
            )

            Row(modifier = Modifier.padding(top = 24.dp)) {
                Text(
                    text = "Already have an account? ",
                    fontFamily = Inter,
                    fontSize = 16.sp,
                    color = Color.Gray
                )
                Text(
                    text = "Sign in",
                    fontFamily = Inter,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.clickable { onSignIn() }
                )
            }
            
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}import androidx.compose.ui.tooling.preview.Preview

@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    WelcomeScreen(onGetStarted = {}, onSignIn = {})
}
