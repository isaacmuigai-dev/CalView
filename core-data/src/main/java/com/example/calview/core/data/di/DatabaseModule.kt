package com.example.calview.core.data.di

import android.content.Context
import androidx.room.Room
import com.example.calview.core.data.local.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "calview_db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    fun provideMealDao(database: AppDatabase): MealDao {
        return database.mealDao()
    }
    
    @Provides
    fun provideDailyLogDao(database: AppDatabase): DailyLogDao {
        return database.dailyLogDao()
    }
    
    @Provides
    fun provideWeightHistoryDao(database: AppDatabase): WeightHistoryDao {
        return database.weightHistoryDao()
    }
    
    @Provides
    fun provideFastingDao(database: AppDatabase): FastingDao {
        return database.fastingDao()
    }

    @Provides
    fun provideGamificationDao(database: AppDatabase): GamificationDao {
        return database.gamificationDao()
    }
    
    @Provides
    fun provideExerciseDao(database: AppDatabase): ExerciseDao {
        return database.exerciseDao()
    }
    
    // Premium feature DAOs
    @Provides
    fun provideStreakFreezeDao(database: AppDatabase): StreakFreezeDao {
        return database.streakFreezeDao()
    }
    
    @Provides
    fun provideWaterReminderDao(database: AppDatabase): WaterReminderDao {
        return database.waterReminderDao()
    }
    
    @Provides
    fun provideSocialChallengeDao(database: AppDatabase): SocialChallengeDao {
        return database.socialChallengeDao()
    }
}
