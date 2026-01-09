package com.example.calview.core.data.di

import android.content.Context
import androidx.room.Room
import com.example.calview.core.data.local.AppDatabase
import com.example.calview.core.data.local.DailyLogDao
import com.example.calview.core.data.local.MealDao
import com.example.calview.core.data.local.WeightHistoryDao
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
}
