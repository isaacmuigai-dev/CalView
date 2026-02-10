package com.example.calview.feature.dashboard

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.res.stringResource
import com.example.calview.core.ui.theme.InterFontFamily
import com.example.calview.core.ui.theme.SpaceGroteskFontFamily
import com.example.calview.feature.dashboard.R

// Initials Avatar Gradients - 16 options for variety
val AvatarGradients = listOf(
    // Blues & Purples
    Brush.linearGradient(listOf(Color(0xFF4A90E2), Color(0xFF9013FE))),
    Brush.linearGradient(listOf(Color(0xFF667EEA), Color(0xFF764BA2))),
    Brush.linearGradient(listOf(Color(0xFF00C6FB), Color(0xFF005BEA))),
    Brush.linearGradient(listOf(Color(0xFF6A11CB), Color(0xFF2575FC))),
    // Greens & Teals
    Brush.linearGradient(listOf(Color(0xFF43E97B), Color(0xFF38F9D7))),
    Brush.linearGradient(listOf(Color(0xFF11998E), Color(0xFF38EF7D))),
    Brush.linearGradient(listOf(Color(0xFF00B09B), Color(0xFF96C93D))),
    // Warm colors
    Brush.linearGradient(listOf(Color(0xFFFA709A), Color(0xFFFEE140))),
    Brush.linearGradient(listOf(Color(0xFFFF0844), Color(0xFFFFB199))),
    Brush.linearGradient(listOf(Color(0xFFF093FB), Color(0xFFF5576C))),
    Brush.linearGradient(listOf(Color(0xFFFF6B6B), Color(0xFFFFE66D))),
    // Sunset & Warm
    Brush.linearGradient(listOf(Color(0xFFFC4A1A), Color(0xFFF7B733))),
    Brush.linearGradient(listOf(Color(0xFFE65C00), Color(0xFFF9D423))),
    // Cool blues & cyans
    Brush.linearGradient(listOf(Color(0xFF2193B0), Color(0xFF6DD5ED))),
    Brush.linearGradient(listOf(Color(0xFF0575E6), Color(0xFF021B79))),
    // Dark & moody
    Brush.linearGradient(listOf(Color(0xFF232526), Color(0xFF414345)))
)

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun GroupsUnifiedProfileSetupScreen(
    viewModel: GroupsProfileViewModel,
    onBack: () -> Unit,
    onProfileCreated: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    
    // Initials logic
    val initials = remember(uiState.firstName, uiState.lastName) {
        val f = uiState.firstName.trim().take(1)
        val l = uiState.lastName.trim().take(1)
        if (f.isEmpty() && l.isEmpty()) "?" else (f + l).uppercase()
    }
    
    // Image picker launcher
    val imagePickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let { viewModel.updateCustomPhotoUri(it) }
    }

    // Pager for colors
    val pagerState = androidx.compose.foundation.pager.rememberPagerState(
        initialPage = uiState.selectedInitialColorIndex,
        pageCount = { AvatarGradients.size }
    )
    
    // Update ViewModel when pager changes
    LaunchedEffect(pagerState.currentPage) {
        viewModel.updateSelectedColorIndex(pagerState.currentPage)
    }

    // Validation
    val isValidLength = uiState.username.length >= 3
    val isFormValid = uiState.firstName.isNotBlank() && 
                      uiState.lastName.isNotBlank() && 
                      isValidLength && 
                      uiState.isUsernameAvailable == true

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(com.example.calview.core.ui.theme.CalViewTheme.gradient)
    ) {
        Scaffold(
            topBar = {
                IconButton(onClick = onBack, modifier = Modifier.padding(8.dp).statusBarsPadding()) {
                    Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back_desc))
                }
            },
            bottomBar = {
                Column(modifier = Modifier.navigationBarsPadding().padding(horizontal = 24.dp, vertical = 24.dp)) {
                    Button(
                        onClick = { 
                            focusManager.clearFocus()
                            viewModel.saveProfile(onSuccess = onProfileCreated) 
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                        ),
                        enabled = isFormValid && !uiState.isSaving
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Text(
                                text = stringResource(R.string.create_profile_action),
                                fontFamily = InterFontFamily,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            },
            containerColor = Color.Transparent
        ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null
                ) { focusManager.clearFocus() }, // Tap outside to clear focus
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.profile_setup_title),
                fontFamily = SpaceGroteskFontFamily,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.profile_setup_subtitle),
                fontFamily = InterFontFamily,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // --- Photo Section ---
            Box(contentAlignment = Alignment.BottomEnd) {
                if (uiState.customPhotoUri != null) {
                    // Show custom uploaded photo
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.foundation.Image(
                            painter = coil.compose.rememberAsyncImagePainter(uiState.customPhotoUri),
                            contentDescription = "Profile photo",
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    }
                } else {
                    // Horizontal pager for avatar selection with initials
                    androidx.compose.foundation.pager.HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .width(120.dp)
                            .height(120.dp),
                        pageSpacing = 16.dp
                    ) { page ->
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(AvatarGradients[page])
                                .clickable { imagePickerLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initials,
                                color = Color.White,
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = InterFontFamily
                            )
                        }
                    }
                }
                
                // Edit Badge
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.background),
                    modifier = Modifier
                        .size(36.dp)
                        .offset(x = 4.dp, y = 4.dp)
                        .clickable { imagePickerLauncher.launch("image/*") }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Edit, 
                            contentDescription = "Edit photo", 
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            
            if (uiState.customPhotoUri == null) {
                Spacer(modifier = Modifier.height(16.dp))
                // Page indicator dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    repeat(AvatarGradients.size) { index ->
                        Box(
                            modifier = Modifier
                                .size(if (pagerState.currentPage == index) 8.dp else 4.dp)
                                .clip(CircleShape)
                                .background(
                                    if (pagerState.currentPage == index) 
                                        MaterialTheme.colorScheme.primary 
                                    else 
                                        MaterialTheme.colorScheme.outlineVariant
                                )
                                .clickable { 
                                    scope.launch { pagerState.animateScrollToPage(index) }
                                }
                        )
                    }
                }
                Text(
                    text = stringResource(R.string.swipe_to_select),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            } else {
                 TextButton(onClick = { viewModel.updateCustomPhotoUri(null) }) {
                    Text(stringResource(R.string.choose_different_photo_action))
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // --- Fields Section ---
            GroupsTextField(
                value = uiState.firstName,
                onValueChange = viewModel::updateFirstName,
                label = stringResource(R.string.first_name_label),
                placeholder = stringResource(R.string.first_name_placeholder),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            Spacer(modifier = Modifier.height(16.dp))
            GroupsTextField(
                value = uiState.lastName,
                onValueChange = viewModel::updateLastName,
                label = stringResource(R.string.last_name_label),
                placeholder = stringResource(R.string.last_name_placeholder),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
             Spacer(modifier = Modifier.height(16.dp))
            GroupsTextField(
                value = uiState.username,
                onValueChange = viewModel::updateUsername,
                label = stringResource(R.string.username_label),
                placeholder = stringResource(R.string.username_placeholder_at),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                trailingIcon = when {
                    uiState.isCheckingUsername -> {
                        { 
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            ) 
                        }
                    }
                    isValidLength && uiState.isUsernameAvailable == true -> {
                        { 
                            Icon(
                                Icons.Default.Check, 
                                contentDescription = "Available", 
                                tint = Color(0xFF4CAF50) 
                            ) 
                        }
                    }
                    isValidLength && uiState.isUsernameAvailable == false -> {
                        { 
                            Icon(
                                Icons.Default.Close, 
                                contentDescription = "Taken", 
                                tint = Color(0xFFF44336) 
                            ) 
                        }
                    }
                    else -> null
                }
            )
            // Status helper text
            when {
                uiState.isCheckingUsername -> {
                    Text(
                        text = stringResource(R.string.username_checking),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp).align(Alignment.Start)
                    )
                }
                isValidLength && uiState.isUsernameAvailable == true -> {
                    Text(
                        text = stringResource(R.string.username_available),
                        color = Color(0xFF4CAF50),
                         fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp).align(Alignment.Start)
                    )
                }
                isValidLength && uiState.isUsernameAvailable == false -> {
                    Text(
                        text = stringResource(R.string.username_taken),
                        color = Color(0xFFF44336),
                         fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp).align(Alignment.Start)
                    )
                }
            }

            if (uiState.errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = uiState.errorMessage.orEmpty(),
                    color = Color(0xFFF44336),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }


            Spacer(modifier = Modifier.height(48.dp))
        }
        }
    }
}

// Keeping helper components needed for the unified screen
@Composable
fun GroupsTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    keyboardOptions: KeyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
         Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(label) },
            placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            trailingIcon = trailingIcon,
            singleLine = true,
            keyboardOptions = keyboardOptions
        )
    }
}
