package com.example.calview.core.data.repository

import com.example.calview.core.data.local.StreakFreezeDao
import com.example.calview.core.data.local.StreakFreezeEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StreakFreezeRepository @Inject constructor(
    private val streakFreezeDao: StreakFreezeDao
) {
    private val monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM")
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun observeCurrentMonthFreezes(): Flow<StreakFreezeEntity?> {
        val currentMonth = LocalDate.now().format(monthFormatter)
        return streakFreezeDao.observeFreezeForMonth(currentMonth)
    }

    suspend fun getRemainingFreezes(): Int {
        val currentMonth = LocalDate.now().format(monthFormatter)
        val freeze = streakFreezeDao.getFreezeForMonth(currentMonth)
        
        return if (freeze != null) {
            (freeze.maxFreezes - freeze.freezesUsed).coerceAtLeast(0)
        } else {
            2 // Default max freezes
        }
    }

    suspend fun hasFreezesAvailable(): Boolean {
         return getRemainingFreezes() > 0
    }

    suspend fun useFreeze(date: LocalDate): Boolean {
        if (!hasFreezesAvailable()) return false

        val currentMonth = LocalDate.now().format(monthFormatter)
        val freeze = streakFreezeDao.getFreezeForMonth(currentMonth) ?: return false

        // Check if already frozen
        val dateStr = date.format(dateFormatter)
        if (freeze.frozenDates.contains(dateStr)) return true

        // Add date
        val newFrozenDates = if (freeze.frozenDates.isEmpty()) {
            dateStr
        } else {
            "${freeze.frozenDates},$dateStr"
        }

        streakFreezeDao.useFreeze(currentMonth, newFrozenDates)
        return true
    }

    suspend fun isDateFrozen(date: LocalDate): Boolean {
        val currentMonth = LocalDate.now().format(monthFormatter)
        val freeze = streakFreezeDao.getFreezeForMonth(currentMonth) ?: return false
        
        val dateStr = date.format(dateFormatter)
        return freeze.frozenDates.split(",").contains(dateStr)
    }
    
    // Helper to init if needed
    suspend fun ensureStreakFreezeInitialized() {
        val currentMonth = LocalDate.now().format(monthFormatter)
        val existing = streakFreezeDao.getFreezeForMonth(currentMonth)
        if (existing == null) {
            streakFreezeDao.insertStreakFreeze(
                StreakFreezeEntity(
                    month = currentMonth,
                    maxFreezes = 2,
                    freezesUsed = 0,
                    frozenDates = ""
                )
            )
        }
    }
}
