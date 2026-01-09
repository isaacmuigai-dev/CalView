package com.example.calview.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [MealEntity::class, DailyLogEntity::class, WeightHistoryEntity::class], 
    version = 5, 
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mealDao(): MealDao
    abstract fun dailyLogDao(): DailyLogDao
    abstract fun weightHistoryDao(): WeightHistoryDao
}
