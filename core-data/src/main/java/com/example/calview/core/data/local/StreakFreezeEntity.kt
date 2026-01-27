package com.example.calview.core.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "streak_freezes")
data class StreakFreezeEntity(
    @PrimaryKey
    val month: String = "", // Format: "yyyy-MM"
    val maxFreezes: Int = 2,
    val freezesUsed: Int = 0,
    val frozenDates: String = "" // Comma separated ISO dates
)
