package com.example.calview.feature.dashboard

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
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

@Composable
fun GroupsNameSetupScreen(
    viewModel: GroupsProfileViewModel,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    ProfileSetupScaffold(
        title = stringResource(R.string.confirm_name_title),
        subtitle = stringResource(R.string.confirm_name_subtitle),
        onBack = onBack,
        onNext = onNext,
        nextEnabled = uiState.firstName.isNotBlank() && uiState.lastName.isNotBlank()
    ) {
        GroupsTextField(
            value = uiState.firstName,
            onValueChange = viewModel::updateFirstName,
            label = stringResource(R.string.first_name_label),
            placeholder = stringResource(R.string.first_name_placeholder)
        )
        Spacer(modifier = Modifier.height(24.dp))
        GroupsTextField(
            value = uiState.lastName,
            onValueChange = viewModel::updateLastName,
            label = stringResource(R.string.last_name_label),
            placeholder = stringResource(R.string.last_name_placeholder)
        )
    }
}

@Composable
fun GroupsUsernameSetupScreen(
    viewModel: GroupsProfileViewModel,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isValidLength = uiState.username.length >= 3

    ProfileSetupScaffold(
        title = stringResource(R.string.create_username_title),
        subtitle = stringResource(R.string.create_username_subtitle),
        onBack = onBack,
        onNext = onNext,
        nextEnabled = isValidLength && uiState.isUsernameAvailable == true
    ) {
        GroupsTextField(
            value = uiState.username,
            onValueChange = viewModel::updateUsername,
            label = stringResource(R.string.username_label),
            placeholder = stringResource(R.string.username_placeholder),
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
                            contentDescription = stringResource(R.string.username_available), 
                            tint = Color(0xFF4CAF50) // Green for available
                        ) 
                    }
                }
                isValidLength && uiState.isUsernameAvailable == false -> {
                    { 
                        Icon(
                            Icons.Default.Close, 
                            contentDescription = "Username taken", 
                            tint = Color(0xFFF44336) // Red for taken
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
                    text = "Checking availability...",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp, start = 4.dp)
                )
            }
            isValidLength && uiState.isUsernameAvailable == true -> {
                Text(
                    text = stringResource(R.string.username_available),
                    color = Color(0xFF4CAF50),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp, start = 4.dp)
                )
            }
            isValidLength && uiState.isUsernameAvailable == false -> {
                Text(
                    text = "Username is already taken",
                    color = Color(0xFFF44336),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp, start = 4.dp)
                )
            }
        }
    }
}


@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun GroupsPhotoSetupScreen(
    viewModel: GroupsProfileViewModel,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val initials = (uiState.firstName.take(1) + uiState.lastName.take(1)).uppercase()

    // Image picker launcher
    val imagePickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let { viewModel.updateCustomPhotoUri(it) }
    }

    // Coroutine scope for pager animations
    val scope = rememberCoroutineScope()

    // Pager state for avatar selection
    val pagerState = androidx.compose.foundation.pager.rememberPagerState(
        initialPage = uiState.selectedInitialColorIndex,
        pageCount = { AvatarGradients.size }
    )

    // Update ViewModel when pager changes
    LaunchedEffect(pagerState.currentPage) {
        viewModel.updateSelectedColorIndex(pagerState.currentPage)
    }

    ProfileSetupScaffold(
        title = stringResource(R.string.add_profile_photo_title),
        subtitle = stringResource(R.string.add_profile_photo_subtitle),
        onBack = onBack,
        onNext = onNext,
        nextEnabled = true
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (uiState.customPhotoUri != null) {
                // Show custom uploaded photo
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Image(
                        painter = coil.compose.rememberAsyncImagePainter(uiState.customPhotoUri),
                        contentDescription = "Profile photo",
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                TextButton(onClick = { viewModel.updateCustomPhotoUri(null) }) {
                    Text("Use initials instead")
                }
            } else {
                // Horizontal pager for avatar selection with initials
                androidx.compose.foundation.pager.HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 90.dp),
                    pageSpacing = 16.dp
                ) { page ->
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .clip(CircleShape)
                            .background(AvatarGradients[page])
                            .then(
                                if (page == pagerState.currentPage) {
                                    Modifier.border(4.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                } else {
                                    Modifier.border(2.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            color = Color.White,
                            fontSize = 64.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = InterFontFamily
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                // Page indicator dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    repeat(AvatarGradients.size) { index ->
                        Box(
                            modifier = Modifier
                                .size(if (pagerState.currentPage == index) 10.dp else 6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (pagerState.currentPage == index) 
                                        MaterialTheme.colorScheme.primary 
                                    else 
                                        MaterialTheme.colorScheme.outlineVariant
                                )
                                .clickable { 
                                    scope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                }
                        )
                    }
                }
                
                Text(
                    text = stringResource(R.string.swipe_to_select),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
                Text(
                    text = stringResource(R.string.or_separator),
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = { imagePickerLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Text(stringResource(R.string.upload_photo_action), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            }
        }
    }
}


@Composable
fun GroupsPhotoConfirmScreen(
    viewModel: GroupsProfileViewModel,
    onBack: () -> Unit,
    onCreateProfile: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val initials = (uiState.firstName.take(1) + uiState.lastName.take(1)).uppercase()

    ProfileSetupScaffold(
        title = stringResource(R.string.confirm_photo_title),
        subtitle = stringResource(R.string.confirm_photo_subtitle),
        onBack = onBack,
        onNext = onCreateProfile,
        nextLabel = stringResource(R.string.create_profile_action),
        nextEnabled = true
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.customPhotoUri != null) {
                    androidx.compose.foundation.Image(
                        painter = coil.compose.rememberAsyncImagePainter(uiState.customPhotoUri),
                        contentDescription = "Profile photo",
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    val safeColorIndex = uiState.selectedInitialColorIndex.coerceIn(0, AvatarGradients.size - 1)
                    Box(
                        modifier = Modifier.fillMaxSize().background(AvatarGradients[safeColorIndex]),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            color = Color.White,
                            fontSize = 72.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = InterFontFamily
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.wrapContentWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Delete, 
                        contentDescription = stringResource(R.string.choose_different_photo_action), 
                        tint = MaterialTheme.colorScheme.error, 
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.choose_different_photo_action), 
                        color = MaterialTheme.colorScheme.error, 
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileSetupScaffold(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    onNext: () -> Unit,
    nextLabel: String = "Next",
    nextEnabled: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
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
                    onClick = onNext,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                    ),
                    enabled = nextEnabled
                ) {
                    Text(
                        text = nextLabel,
                        fontFamily = InterFontFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = title,
                fontFamily = SpaceGroteskFontFamily,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                fontFamily = InterFontFamily,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(48.dp))
            
            content()

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun GroupsTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
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
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )
    }
}
