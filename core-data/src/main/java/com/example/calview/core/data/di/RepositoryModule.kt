package com.example.calview.core.data.di

import com.example.calview.core.data.repository.DailyLogRepository
import com.example.calview.core.data.repository.DailyLogRepositoryImpl
import com.example.calview.core.data.repository.MealRepository
import com.example.calview.core.data.repository.MealRepositoryImpl
import com.example.calview.core.data.repository.UserPreferencesRepository
import com.example.calview.core.data.repository.UserPreferencesRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMealRepository(
        mealRepositoryImpl: MealRepositoryImpl
    ): MealRepository

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(
        userPreferencesRepositoryImpl: UserPreferencesRepositoryImpl
    ): UserPreferencesRepository
    
    @Binds
    @Singleton
    abstract fun bindDailyLogRepository(
        dailyLogRepositoryImpl: DailyLogRepositoryImpl
    ): DailyLogRepository
}
