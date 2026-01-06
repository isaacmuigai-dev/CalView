package com.example.calview.feature.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calview.core.ui.theme.Inter

/**
 * Data class for language option.
 */
data class LanguageItem(
    val code: String,
    val name: String,
    val flag: String
)

/**
 * List of supported languages.
 */
val supportedLanguages = listOf(
    LanguageItem("en", "English", "🇬🇧"),
    LanguageItem("es", "Español", "🇪🇸"),
    LanguageItem("fr", "Français", "🇫🇷"),
    LanguageItem("de", "Deutsch", "🇩🇪"),
    LanguageItem("it", "Italiano", "🇮🇹"),
    LanguageItem("pt", "Português", "🇧🇷"),
    LanguageItem("ru", "Русский", "🇷🇺"),
    LanguageItem("zh", "中文", "🇨🇳"),
    LanguageItem("ja", "日本語", "🇯🇵"),
    LanguageItem("ko", "한국어", "🇰🇷"),
    LanguageItem("ar", "العربية", "🇸🇦"),
    LanguageItem("hi", "हिन्दी", "🇮🇳")
)

/**
 * Language Selector screen - standalone screen for changing app language.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelectorScreen(
    currentLanguage: String = "en",
    onBack: () -> Unit,
    onLanguageSelected: (String) -> Unit
) {
    var selectedLanguage by remember { mutableStateOf(currentLanguage) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Language",
                        fontFamily = Inter,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(supportedLanguages) { language ->
                    LanguageOptionCard(
                        language = language,
                        isSelected = language.code == selectedLanguage,
                        onClick = { selectedLanguage = language.code }
                    )
                }
            }
            
            // Save button
            Button(
                onClick = { 
                    onLanguageSelected(selectedLanguage)
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = "Save",
                    fontFamily = Inter,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun LanguageOptionCard(
    language: LanguageItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = language.flag,
                    fontSize = 24.sp
                )
                Text(
                    text = language.name,
                    fontFamily = Inter,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                )
            }
            
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
