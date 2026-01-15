package com.example.calview.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calview.core.ui.components.CalAICard
import com.example.calview.feature.dashboard.R
import androidx.compose.ui.res.stringResource

/**
 * Open Source Licenses Screen
 * Displays attribution and license information for third-party libraries and models
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpenSourceLicensesScreen(
    onBack: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val uriHandler = LocalUriHandler.current
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.open_source_licenses_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            // Introduction text
            Text(
                text = stringResource(R.string.open_source_intro),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // TensorFlow Lite
            LicenseCard(
                name = "TensorFlow Lite",
                description = "On-device machine learning framework for mobile and embedded devices.",
                license = "Apache License 2.0",
                url = "https://www.tensorflow.org/lite",
                onLinkClick = { uriHandler.openUri("https://www.tensorflow.org/lite") }
            )
            
            // Google AIY Vision Food Model
            LicenseCard(
                name = "Google AIY Vision Food Classifier",
                description = "TFLite model for classifying 2024 types of food. Based on MobileNet V1 architecture.",
                license = "Apache License 2.0",
                url = "https://www.kaggle.com/models/google/aiy",
                onLinkClick = { uriHandler.openUri("https://www.kaggle.com/models/google/aiy") }
            )
            
            // ML Kit
            LicenseCard(
                name = "Google ML Kit",
                description = "Machine learning SDKs for object detection and image analysis.",
                license = "Apache License 2.0",
                url = "https://developers.google.com/ml-kit",
                onLinkClick = { uriHandler.openUri("https://developers.google.com/ml-kit") }
            )
            
            // CameraX
            LicenseCard(
                name = "AndroidX CameraX",
                description = "Camera API for Android that simplifies camera development.",
                license = "Apache License 2.0",
                url = "https://developer.android.com/training/camerax",
                onLinkClick = { uriHandler.openUri("https://developer.android.com/training/camerax") }
            )
            
            // Jetpack Compose
            LicenseCard(
                name = "Jetpack Compose",
                description = "Modern UI toolkit for building native Android UI.",
                license = "Apache License 2.0",
                url = "https://developer.android.com/jetpack/compose",
                onLinkClick = { uriHandler.openUri("https://developer.android.com/jetpack/compose") }
            )
            
            // Coil
            LicenseCard(
                name = "Coil",
                description = "Image loading library for Android backed by Kotlin Coroutines.",
                license = "Apache License 2.0",
                url = "https://coil-kt.github.io/coil/",
                onLinkClick = { uriHandler.openUri("https://coil-kt.github.io/coil/") }
            )
            
            // Apache 2.0 License Text
            Spacer(modifier = Modifier.height(8.dp))
            
            CalAICard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.apache_license_2_0),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.apache_license_text),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun LicenseCard(
    name: String,
    description: String,
    license: String,
    url: String,
    onLinkClick: () -> Unit
) {
    CalAICard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onLinkClick() }
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = "Open link",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = description,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = license,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
