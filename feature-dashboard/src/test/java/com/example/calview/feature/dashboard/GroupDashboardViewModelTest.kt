package com.example.calview.feature.dashboard

import androidx.lifecycle.SavedStateHandle
import com.example.calview.core.data.model.GroupDto
import com.example.calview.core.data.model.GroupMessageDto
import com.example.calview.core.data.repository.GroupsRepository
import com.example.calview.core.data.repository.UserPreferencesRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GroupDashboardViewModelTest {

    private lateinit var viewModel: GroupDashboardViewModel
    private lateinit var groupsRepository: GroupsRepository
    private lateinit var userPreferencesRepository: UserPreferencesRepository
    private lateinit var savedStateHandle: SavedStateHandle

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        groupsRepository = mockk(relaxed = true)
        userPreferencesRepository = mockk(relaxed = true)
        savedStateHandle = SavedStateHandle()

        // Mock UserPreferences flows
        coEvery { userPreferencesRepository.groupsFirstName } returns MutableStateFlow("Test")
        coEvery { userPreferencesRepository.groupsLastName } returns MutableStateFlow("User")
        coEvery { userPreferencesRepository.groupsProfilePhotoUrl } returns MutableStateFlow("http://photo.url")

        viewModel = GroupDashboardViewModel(
            groupsRepository = groupsRepository,
            userPreferencesRepository = userPreferencesRepository,
            savedStateHandle = savedStateHandle
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `sendMessage success clears input and resets sending state`() = runTest {
        // Given
        val groupId = "group1"
        val group = GroupDto(id = groupId, name = "Test Group")
        viewModel.onGroupSelected(group)
        viewModel.onInputTextChanged("Hello World")
        
        coEvery { groupsRepository.sendMessage(any(), any(), any(), any()) } returns "messageId123"

        // When
        viewModel.sendMessage()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { groupsRepository.sendMessage(groupId, "Hello World", null, null) }
        assertEquals("", viewModel.uiState.value.inputText)
        assertFalse(viewModel.uiState.value.isSending)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `sendMessage failure shows error message`() = runTest {
        // Given
        val groupId = "group1"
        val group = GroupDto(id = groupId, name = "Test Group")
        viewModel.onGroupSelected(group)
        viewModel.onInputTextChanged("Fail Me")
        
        coEvery { groupsRepository.sendMessage(any(), any(), any(), any()) } returns null

        // When
        viewModel.sendMessage()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { groupsRepository.sendMessage(groupId, "Fail Me", null, null) }
        assertEquals("Fail Me", viewModel.uiState.value.inputText) // Input should persist on failure logic? 
        // Actually, logic clears input only if messageId != null.
        // Let's verify that.
        assertFalse(viewModel.uiState.value.isSending)
        assertEquals("Failed to send message", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `sendMessage exception handles crash`() = runTest {
        // Given
        val groupId = "group1"
        val group = GroupDto(id = groupId, name = "Test Group")
        viewModel.onGroupSelected(group)
        viewModel.onInputTextChanged("Crash Me")
        
        coEvery { groupsRepository.sendMessage(any(), any(), any(), any()) } throws RuntimeException("Network error")

        // When
        viewModel.sendMessage()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertFalse(viewModel.uiState.value.isSending)
        assertTrue(viewModel.uiState.value.errorMessage?.contains("Network error") == true)
    }
}
