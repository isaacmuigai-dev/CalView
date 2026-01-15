package com.example.calview.core.ml.di

import android.content.Context
import com.example.calview.core.ml.FoodClassifier
import com.example.calview.core.ml.FoodObjectDetector
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MLModule {
    
    @Provides
    @Singleton
    fun provideFoodClassifier(
        @ApplicationContext context: Context
    ): FoodClassifier {
        return FoodClassifier(context)
    }
    
    @Provides
    @Singleton
    fun provideFoodObjectDetector(): FoodObjectDetector {
        return FoodObjectDetector()
    }
}
