package com.example.calview.di

import android.content.Context
import androidx.room.Room
import com.example.calview.data.local.AppDatabase
import com.example.calview.data.local.MealDao
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
        ).build()
    }

    @Provides
    fun provideMealDao(database: AppDatabase): MealDao {
        return database.mealDao()
    }
}
