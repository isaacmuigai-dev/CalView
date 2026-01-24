package com.example.calview.core.data.repository

import com.example.calview.core.data.local.StreakFreezeDao
import com.example.calview.core.data.local.StreakFreezeEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StreakFreezeRepository @Inject constructor(
    private val streakFreezeDao: StreakFreezeDao,
    private val authRepository: AuthRepository,
    private val firestoreRepository: FirestoreRepository
) {
    private val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO + kotlinx.coroutines.SupervisorJob())
    private val monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM")
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun observeCurrentMonthFreezes(): Flow<StreakFreezeEntity?> {
        val currentMonth = LocalDate.now().format(monthFormatter)
        return streakFreezeDao.observeFreezeForMonth(currentMonth)
    }

    /**
     * Calculates the current streak by merging meal dates and frozen dates.
     * @param mealDates List of dates where meals were logged.
     * @return Flow of Int representing the current streak.
     */
    fun getStreakData(mealDates: List<LocalDate>): Flow<Int> = observeCurrentMonthFreezes().map { freeze ->
        val frozenDates = freeze?.frozenDates?.split(",")
            ?.filter { it.isNotEmpty() }
            ?.map { LocalDate.parse(it, dateFormatter) }
            ?: emptyList()

        val allActiveDates = (mealDates + frozenDates).distinct().sortedDescending()
        
        var streak = 0
        var checkDate = LocalDate.now()
        
        // If nothing today, check if yesterday was the last active day
        if (!allActiveDates.contains(checkDate)) {
            checkDate = checkDate.minusDays(1)
        }

        for (date in allActiveDates) {
            if (date == checkDate) {
                streak++
                checkDate = checkDate.minusDays(1)
            } else if (date.isBefore(checkDate)) {
                break
            }
        }
        streak
    }

    /**
     * Calculates the best streak ever by merging all meal dates and all historical frozen dates.
     * Note: Current implementation only has monthly freezes. For a true best streak,
     * we would need historical freeze data.
     */
    fun calculateBestStreak(mealDates: List<LocalDate>, currentStreak: Int): Flow<Int> = observeCurrentMonthFreezes().map { freeze ->
        val frozenDates = freeze?.frozenDates?.split(",")
            ?.filter { it.isNotEmpty() }
            ?.map { LocalDate.parse(it, dateFormatter) }
            ?: emptyList()

        val allActiveDates = (mealDates + frozenDates).distinct().sorted()
        
        var best = currentStreak
        var currentTemp = 0
        var lastDate: LocalDate? = null

        for (date in allActiveDates) {
            if (lastDate == null || date == lastDate.plusDays(1)) {
                currentTemp++
            } else {
                best = maxOf(best, currentTemp)
                currentTemp = 1
            }
            lastDate = date
        }
        maxOf(best, currentTemp)
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
        
        // Sync to cloud
        streakFreezeDao.getFreezeForMonth(currentMonth)?.let { updated ->
            syncToCloud(updated)
        }
        
        return true
    }

    private fun syncToCloud(freeze: StreakFreezeEntity) {
        val userId = authRepository.getUserId()
        if (userId.isNotEmpty()) {
            scope.launch {
                try {
                    firestoreRepository.saveStreakFreeze(userId, freeze)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
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
            // Sync initial state
            streakFreezeDao.getFreezeForMonth(currentMonth)?.let { syncToCloud(it) }
        }
    }

    suspend fun restoreFromCloud(): Boolean {
        val userId = authRepository.getUserId()
        if (userId.isEmpty()) return false
        
        return try {
            val cloudFreezes = firestoreRepository.getStreakFreezes(userId)
            if (cloudFreezes.isNotEmpty()) {
                cloudFreezes.forEach { freeze ->
                    streakFreezeDao.insertStreakFreeze(freeze)
                }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun clearAllData() {
        streakFreezeDao.deleteAll()
    }
}
