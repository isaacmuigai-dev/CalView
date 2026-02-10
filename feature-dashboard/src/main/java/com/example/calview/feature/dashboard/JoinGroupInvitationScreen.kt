package com.example.calview.feature.dashboard

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.calview.core.ui.theme.InterFontFamily
import com.example.calview.core.ui.theme.SpaceGroteskFontFamily
import com.example.calview.feature.dashboard.R
import kotlinx.coroutines.launch
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalFocusManager
import coil.compose.AsyncImage
import androidx.compose.animation.*
import com.example.calview.core.data.model.GroupDto

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun JoinGroupInvitationScreen(
    onBack: () -> Unit,
    onDone: () -> Unit,
    viewModel: JoinGroupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val pagerState = rememberPagerState(pageCount = { 4 }) // 3 instructional + 1 join page
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // Navigate when successfully joined
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onDone()
        }
    }
    
    // Auto-scroll to page 4 if code is provided via deep link
    LaunchedEffect(uiState.inviteCode) {
        if (uiState.inviteCode.length == 6 && pagerState.currentPage < 3) {
            pagerState.animateScrollToPage(3)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(com.example.calview.core.ui.theme.CalViewTheme.gradient)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                IconButton(
                    onClick = onBack, 
                    modifier = Modifier.padding(8.dp).statusBarsPadding()
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back_desc), tint = MaterialTheme.colorScheme.onBackground)
                }
            },
        bottomBar = {
            Column(modifier = Modifier.navigationBarsPadding().padding(horizontal = 24.dp, vertical = 24.dp)) {
                Button(
                    onClick = {
                        if (pagerState.currentPage < 3) {
                            // Not on last page - go to next page
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            // On last page (page 4) - Join Group
                            focusManager.clearFocus()
                            viewModel.joinGroup()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    enabled = if (pagerState.currentPage < 3) true else uiState.groupPreview != null && !uiState.isJoining
                ) {
                    if (uiState.isJoining) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text(
                            text = if (pagerState.currentPage < 3) 
                                stringResource(R.string.continue_action) 
                            else 
                                stringResource(R.string.join_btn),
                            fontFamily = InterFontFamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.ask_leader_invite_title),
                fontFamily = SpaceGroteskFontFamily,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 34.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (pagerState.currentPage < 3) 
                    stringResource(R.string.ask_leader_invite_subtitle)
                else 
                    "Enter the 6-digit code shared with you",
                fontFamily = InterFontFamily,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))

            // Pager Section - Only 3 instructional pages
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                userScrollEnabled = !uiState.isJoining
            ) { page ->
                if (page < 3) {
                    InvitationStepCard(page = page)
                } else {
                    JoinWithCodePage(viewModel = viewModel, uiState = uiState)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Page Indicator - Only 3 dots
            Row(
                modifier = Modifier.padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(4) { index ->
                    Box(
                        modifier = Modifier
                            .size(if (pagerState.currentPage == index) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(if (pagerState.currentPage == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
                    )
                }
            }
        }
        }
    }
}

@Composable
private fun JoinWithCodePage(
    viewModel: JoinGroupViewModel,
    uiState: JoinGroupUiState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = uiState.inviteCode,
            onValueChange = viewModel::onInviteCodeChanged,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            placeholder = { Text(stringResource(R.string.enter_code_placeholder), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            textStyle = MaterialTheme.typography.headlineMedium.copy(
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Ascii,
                autoCorrect = false
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.TopCenter) {
            AnimatedContent(
                targetState = Triple(uiState.isLoadingPreview, uiState.groupPreview, uiState.previewError),
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "preview_content"
            ) { (loading, group, error) ->
                when {
                    loading -> {
                        CircularProgressIndicator(modifier = Modifier.padding(top = 48.dp))
                    }
                    group != null -> {
                        GroupPreviewCard(group = group)
                    }
                    error != null -> {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 48.dp).padding(horizontal = 24.dp)
                        )
                    }
                    else -> {
                        Column(
                            modifier = Modifier.padding(top = 48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.QrCode,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.outlineVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Waiting for code...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GroupPreviewCard(group: GroupDto) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (!group.photoUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = group.photoUrl,
                        contentDescription = "Group photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Text(
                        text = group.name.take(1).uppercase(),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = group.name,
                fontFamily = SpaceGroteskFontFamily,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (!group.description.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = group.description.orEmpty(),
                    fontFamily = InterFontFamily,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Group,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Ready to join this group",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
@Composable
private fun InvitationStepCard(page: Int) {
    val title = when (page) {
        0 -> stringResource(R.string.invitation_step_1_title)
        1 -> stringResource(R.string.invitation_step_2_title)
        else -> stringResource(R.string.invitation_step_3_title)
    }
    val description = when (page) {
        0 -> stringResource(R.string.invitation_step_1_desc)
        1 -> stringResource(R.string.invitation_step_2_desc)
        else -> stringResource(R.string.invitation_step_3_desc)
    }
    val icon = when (page) {
        0 -> Icons.Default.Settings
        1 -> Icons.Default.Group
        else -> Icons.Default.Link
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp, horizontal = 4.dp),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = title,
                fontFamily = SpaceGroteskFontFamily,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = description,
                fontFamily = InterFontFamily,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
            Spacer(modifier = Modifier.height(48.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.instruction_graphic_desc),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        fontSize = 12.sp,
                        fontFamily = InterFontFamily
                    )
                }
            }
        }
    }
}
