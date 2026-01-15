package com.example.calview.core.data.state

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Shared state holder for the selected date across the app.
 * When user selects a date in Dashboard, Progress screen will also show that date's data.
 */
@Singleton
class SelectedDateHolder @Inject constructor() {
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()
    
    fun setDate(date: LocalDate) {
        _selectedDate.value = date
    }
    
    fun resetToToday() {
        _selectedDate.value = LocalDate.now()
    }
    
    fun isToday(): Boolean {
        return _selectedDate.value == LocalDate.now()
    }
}
