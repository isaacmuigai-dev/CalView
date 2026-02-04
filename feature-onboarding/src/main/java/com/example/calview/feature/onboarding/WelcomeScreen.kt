package com.example.calview.feature.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calview.core.ui.components.CalAIButton
import com.example.calview.core.ui.theme.InterFontFamily
import com.example.calview.feature.onboarding.R
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.Image

/**
 * Welcome screen matching the "Calorie tracking made easy" design.
 * Features:
 * - Language selector (top-right)
 * - Phone mockup with nutrition UI screenshot (Vector Style)
 * - "Calorie tracking made easy" title
 * - "Get Started" button
 * - "Already have an account? Sign in" link
 */
@Composable
fun WelcomeScreen(
    onGetStarted: () -> Unit,
    onSignIn: () -> Unit,
    isLoading: Boolean = false,
    selectedLanguage: LanguageOption = supportedLanguages.first(),
    onLanguageSelected: (LanguageOption) -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(com.example.calview.core.ui.theme.CalViewTheme.gradient)
            .statusBarsPadding() // Handle edge-to-edge for status bar
            .navigationBarsPadding() // Handle edge-to-edge for navigation bar
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            // Language selector at top-right
            LanguageSelector(
                selectedLanguage = selectedLanguage,
                onLanguageSelected = onLanguageSelected,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 16.dp)
            )

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.weight(1f))

                // Bottom section: Title, Subtitle, Button
                Column(
                    modifier = Modifier.wrapContentHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Title
                    Text(
                        text = stringResource(R.string.welcome_title),
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp, // Slightly smaller to fit if needed
                        lineHeight = 40.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Subtitle
                    Text(
                        text = stringResource(R.string.welcome_subtitle),
                        fontFamily = InterFontFamily,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Get Started button
                    CalAIButton(
                        text = stringResource(R.string.get_started),
                        onClick = onGetStarted
                    )

                    // Sign in link
                    Row(
                        modifier = Modifier.padding(top = 24.dp, bottom = 48.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(R.string.already_have_account),
                            fontFamily = InterFontFamily,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val signInDesc = stringResource(R.string.sign_in_semantics)
                        Text(
                            text = stringResource(R.string.sign_in),
                            fontFamily = InterFontFamily,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground,
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier
                                .clickable { onSignIn() }
                                .semantics {
                                    role = Role.Button
                                    contentDescription = signInDesc
                                }
                        )
                    }
                }
            }
        }

        // Loading Overlay
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(enabled = false) { },
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }
    }
}

/**
 * Phone mockup showing the nutrition analysis UI.
 * Vector icon style: Transparent backgrounds, outlined borders.
 */
@Composable
fun PhoneMockup(
    modifier: Modifier = Modifier
) {
    val borderColor = MaterialTheme.colorScheme.onBackground

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Phone frame - Outlined
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .rotate(-5f),
            shape = RoundedCornerShape(32.dp),
            color = Color.Transparent,
            border = BorderStroke(4.dp, borderColor)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp) // Bezel spacing
            ) {
                // Inner Screen Outline
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Transparent,
                    border = BorderStroke(2.dp, borderColor.copy(alpha = 0.5f))
                ) {
                    // Actual scanning UI Image
                    Image(
                        painter = painterResource(id = com.example.calview.feature.onboarding.R.drawable.scanner_ui_mockup),
                        contentDescription = stringResource(R.string.food_scanner_ui),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    WelcomeScreen(
        onGetStarted = {},
        onSignIn = {},
        onLanguageSelected = {}
    )
}
