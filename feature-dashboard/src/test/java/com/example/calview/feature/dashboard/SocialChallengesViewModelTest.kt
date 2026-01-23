package com.example.calview.feature.dashboard

import com.example.calview.core.data.local.SocialChallengeEntity
import com.example.calview.core.data.local.SocialChallengeType
import com.example.calview.core.data.repository.SocialChallengeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Unit tests for SocialChallengesViewModel
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SocialChallengesViewModelTest {
    
    @Mock
    private lateinit var socialChallengeRepository: SocialChallengeRepository
    
    private lateinit var viewModel: SocialChallengesViewModel
    private val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        
        // Setup default mock responses
        whenever(socialChallengeRepository.getCurrentUserId()).thenReturn("test_user_id")
        whenever(socialChallengeRepository.observeUserChallenges()).thenReturn(flowOf(emptyList()))
        
        viewModel = SocialChallengesViewModel(socialChallengeRepository)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `initial state has correct defaults`() = runTest {
        // Given viewmodel is created
        advanceUntilIdle()
        
        // When
        val state = viewModel.uiState.value
        
        // Then
        assertEquals("test_user_id", state.currentUserId)
        assertTrue(state.challenges.isEmpty())
        assertFalse(state.isLoading)
    }
    
    @Test
    fun `loadChallenges updates state with challenges`() = runTest {
        // Given
        val challenge = SocialChallengeEntity(
            id = "challenge1",
            title = "Test Challenge",
            description = "Test Description",
            type = SocialChallengeType.LOGGING.name,
            targetValue = 7,
            startDate = System.currentTimeMillis(),
            endDate = System.currentTimeMillis() + 604800000,
            creatorId = "test_user_id",
            creatorName = "Test User",
            isActive = true,
            inviteCode = "ABC123"
        )
        whenever(socialChallengeRepository.observeUserChallenges()).thenReturn(flowOf(listOf(challenge)))
        
        // Re-create viewmodel to pick up new mock
        viewModel = SocialChallengesViewModel(socialChallengeRepository)
        advanceUntilIdle()
        
        // When
        val state = viewModel.uiState.value
        
        // Then
        assertEquals(1, state.challenges.size)
        assertEquals("Test Challenge", state.challenges[0].title)
    }
    
    @Test
    fun `createChallenge calls repository`() = runTest {
        // Given
        val newChallenge = SocialChallengeEntity(
            id = "new_challenge",
            title = "New Challenge",
            description = "",
            type = SocialChallengeType.LOGGING.name,
            targetValue = 7,
            startDate = System.currentTimeMillis(),
            endDate = System.currentTimeMillis() + 604800000,
            creatorId = "test_user_id",
            creatorName = "Test User",
            isActive = true,
            inviteCode = "XYZ789"
        )
        whenever(socialChallengeRepository.createChallenge(any(), any(), any(), any(), any()))
            .thenReturn(newChallenge)
        
        advanceUntilIdle()
        
        // When
        viewModel.createChallenge("New Challenge", SocialChallengeType.LOGGING, 7)
        advanceUntilIdle()
        
        // Then
        verify(socialChallengeRepository).createChallenge(
            title = "New Challenge",
            description = "",
            type = SocialChallengeType.LOGGING,
            targetValue = any(),
            durationDays = 7
        )
    }
    
    @Test
    fun `joinChallenge calls repository with code`() = runTest {
        // Given
        val challenge = SocialChallengeEntity(
            id = "joined_challenge",
            title = "Joined Challenge",
            description = "",
            type = SocialChallengeType.WATER.name,
            targetValue = 14,
            startDate = System.currentTimeMillis(),
            endDate = System.currentTimeMillis() + 1209600000,
            creatorId = "other_user",
            creatorName = "Other User",
            isActive = true,
            inviteCode = "JOIN01"
        )
        whenever(socialChallengeRepository.joinChallenge("JOIN01"))
            .thenReturn(Result.success(challenge))
        
        advanceUntilIdle()
        
        // When
        viewModel.joinChallenge("JOIN01")
        advanceUntilIdle()
        
        // Then
        verify(socialChallengeRepository).joinChallenge("JOIN01")
    }
    
    @Test
    fun `leaveChallenge calls repository`() = runTest {
        // Given
        advanceUntilIdle()
        
        // When
        viewModel.leaveChallenge("challenge_id")
        advanceUntilIdle()
        
        // Then
        verify(socialChallengeRepository).leaveChallenge("challenge_id")
    }
    
    @Test
    fun `shareChallenge calls repository`() = runTest {
        // Given
        val challenge = SocialChallengeEntity(
            id = "share_challenge",
            title = "Share Challenge",
            description = "",
            type = SocialChallengeType.STREAK.name,
            targetValue = 7,
            startDate = System.currentTimeMillis(),
            endDate = System.currentTimeMillis() + 604800000,
            creatorId = "test_user_id",
            creatorName = "Test User",
            isActive = true,
            inviteCode = "SHARE1"
        )
        whenever(socialChallengeRepository.getShareLink(any())).thenReturn("https://calview.app/challenge/SHARE1")
        
        advanceUntilIdle()
        
        // When
        viewModel.shareChallenge(challenge)
        advanceUntilIdle()
        
        // Then
        verify(socialChallengeRepository).getShareLink(challenge)
    }
    
    @Test
    fun `currentUserId is fetched on init`() = runTest {
        // Given
        whenever(socialChallengeRepository.getCurrentUserId()).thenReturn("custom_user_123")
        
        // Re-create with new mock
        viewModel = SocialChallengesViewModel(socialChallengeRepository)
        advanceUntilIdle()
        
        // Then
        assertEquals("custom_user_123", viewModel.uiState.value.currentUserId)
    }
}
