package com.example.calview.feature.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calview.core.ui.theme.InterFontFamily

/**
 * Language data class containing language info.
 */
data class LanguageOption(
    val code: String,
    val name: String,
    val flag: String // Emoji flag
)

/**
 * List of supported languages matching the design.
 */
val supportedLanguages = listOf(
    LanguageOption("EN", "English", "🇺🇸"),
    LanguageOption("ES", "Español", "🇪🇸"),
    LanguageOption("PT", "Português", "🇧🇷"),
    LanguageOption("FR", "Français", "🇫🇷"),
    LanguageOption("DE", "Deutsch", "🇩🇪"),
    LanguageOption("IT", "Italiano", "🇮🇹"),
    LanguageOption("HI", "Hindi", "🇮🇳"),
    LanguageOption("JA", "日本語", "🇯🇵"),
    LanguageOption("ZH", "中文", "🇨🇳"),
    LanguageOption("KO", "한국어", "🇰🇷"),
    LanguageOption("AR", "العربية", "🇸🇦"),
    LanguageOption("RU", "Русский", "🇷🇺"),
    LanguageOption("SW", "Kiswahili", "🇰🇪")
)

/**
 * Language selector button that opens the language picker bottom sheet.
 * Shows current language with flag and code.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelector(
    selectedLanguage: LanguageOption = supportedLanguages.first(),
    onLanguageSelected: (LanguageOption) -> Unit,
    modifier: Modifier = Modifier
) {
    var showLanguageSheet by remember { mutableStateOf(false) }
    
    // Selector button
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        modifier = modifier.clickable { showLanguageSheet = true }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = selectedLanguage.flag,
                fontSize = 18.sp
            )
            Text(
                text = selectedLanguage.code,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = "Select language",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
    
    // Language picker bottom sheet
    if (showLanguageSheet) {
        LanguagePickerBottomSheet(
            selectedLanguage = selectedLanguage,
            onLanguageSelected = { language ->
                onLanguageSelected(language)
                showLanguageSheet = false
            },
            onDismiss = { showLanguageSheet = false }
        )
    }
}

/**
 * Full-screen language picker bottom sheet matching the design.
 * Shows "Choose Language" header with X button, and list of language options.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguagePickerBottomSheet(
    selectedLanguage: LanguageOption,
    onLanguageSelected: (LanguageOption) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 24.dp, bottom = 32.dp)
        ) {
            // Header with title and close button
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Choose Language",
                fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.CenterStart)
                )
                
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Language options list
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                supportedLanguages.forEach { language ->
                    LanguageOptionButton(
                        language = language,
                        isSelected = language.code == selectedLanguage.code,
                        onClick = { onLanguageSelected(language) }
                    )
                }
            }
        }
    }
}

/**
 * Individual language option button.
 * Selected language has black background, unselected have white with gray border.
 */
@Composable
fun LanguageOptionButton(
    language: LanguageOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(28.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = language.flag,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = language.name,
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
