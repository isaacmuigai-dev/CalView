package com.example.calview.core.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StreakFreezeDao {
    @Query("SELECT * FROM streak_freezes WHERE month = :month")
    fun observeFreezeForMonth(month: String): Flow<StreakFreezeEntity?>

    @Query("SELECT * FROM streak_freezes")
    fun observeAllFreezes(): Flow<List<StreakFreezeEntity>>

    @Query("SELECT * FROM streak_freezes WHERE month = :month")
    suspend fun getFreezeForMonth(month: String): StreakFreezeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStreakFreeze(streakFreeze: StreakFreezeEntity)

    @Query("UPDATE streak_freezes SET freezesUsed = freezesUsed + 1, frozenDates = :frozenDates WHERE month = :month")
    suspend fun useFreeze(month: String, frozenDates: String)

    @Query("DELETE FROM streak_freezes")
    suspend fun deleteAll()
}
