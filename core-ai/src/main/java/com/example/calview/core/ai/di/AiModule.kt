package com.example.calview.core.ai.di

import com.example.calview.core.ai.ExerciseParsingService
import com.example.calview.core.ai.FoodAnalysisService
import com.example.calview.core.ai.GeminiExerciseParsingService
import com.example.calview.core.ai.GeminiFoodAnalysisService
import com.example.calview.core.ai.GeminiNutritionRecommendationService
import com.example.calview.core.ai.NutritionRecommendationService
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AiModule {

    @Provides
    @Singleton
    fun provideGenerativeModel(): GenerativeModel {
        // Initialize using Firebase AI Logic SDK with Gemini Developer API backend
        return Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel("gemini-2.5-flash")
    }

    @Provides
    @Singleton
    fun provideFoodAnalysisService(
        generativeModel: GenerativeModel
    ): FoodAnalysisService {
        return GeminiFoodAnalysisService(generativeModel)
    }

    @Provides
    @Singleton
    fun provideNutritionRecommendationService(
        generativeModel: GenerativeModel
    ): NutritionRecommendationService {
        return GeminiNutritionRecommendationService(generativeModel)
    }
    
    @Provides
    @Singleton
    fun provideExerciseParsingService(
        generativeModel: GenerativeModel
    ): ExerciseParsingService {
        return GeminiExerciseParsingService(generativeModel)
    }
}
