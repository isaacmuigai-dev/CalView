package com.example.calview.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        MealEntity::class, 
        DailyLogEntity::class, 
        WeightHistoryEntity::class, 
        FastingSessionEntity::class,
        ChallengeEntity::class,
        BadgeEntity::class,
        // Premium features
        StreakFreezeEntity::class,
        WaterReminderSettingsEntity::class,
        SocialChallengeEntity::class,
        ChallengeParticipantEntity::class
    ], 
    version = 8, 
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mealDao(): MealDao
    abstract fun dailyLogDao(): DailyLogDao
    abstract fun weightHistoryDao(): WeightHistoryDao
    abstract fun fastingDao(): FastingDao
    abstract fun gamificationDao(): GamificationDao
    
    // Premium feature DAOs
    abstract fun streakFreezeDao(): StreakFreezeDao
    abstract fun waterReminderDao(): WaterReminderDao
    abstract fun socialChallengeDao(): SocialChallengeDao
}
