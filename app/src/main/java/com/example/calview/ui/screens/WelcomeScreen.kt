package com.example.calview.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calview.ui.components.CalAIButton

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
                fontFamily = FontFamily.SansSerif,
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
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 16.sp,
                    color = Color.Gray
                )
                Text(
                    text = "Sign in",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.clickable { onSignIn() }
                )
            }
            
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    WelcomeScreen(onGetStarted = {}, onSignIn = {})
}
