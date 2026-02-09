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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun JoinGroupInvitationScreen(
    onBack: () -> Unit,
    onDone: () -> Unit,
    viewModel: JoinGroupViewModel = hiltViewModel()
) {
    val pagerState = rememberPagerState(pageCount = { 3 }) // Only 3 instructional pages
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
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
                        if (pagerState.currentPage < 2) {
                            // Not on last page - go to next page
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            // On last page (page 3) - Done, go back to GroupsIntroScreen
                            onDone()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(
                        text = if (pagerState.currentPage < 2) 
                            stringResource(R.string.continue_action) 
                        else 
                            stringResource(R.string.done_action),
                        fontFamily = InterFontFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
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
                text = stringResource(R.string.ask_leader_invite_subtitle),
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
                    .weight(1f)
            ) { page ->
                InvitationStepCard(page = page)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Page Indicator - Only 3 dots
            Row(
                modifier = Modifier.padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(3) { index ->
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
