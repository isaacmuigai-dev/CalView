package com.example.calview.ai.di

import com.example.calview.ai.FoodAnalysisService
import com.example.calview.ai.GeminiFoodAnalysisService
import com.google.ai.client.generativeai.GenerativeModel
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
        return GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = "AIzaSyAWUnVT_UwvfXoZXDvAbV5AX-HnDumod0c"
        )
    }

    @Provides
    @Singleton
    fun provideFoodAnalysisService(
        generativeModel: GenerativeModel
    ): FoodAnalysisService {
        return GeminiFoodAnalysisService(generativeModel)
    }
}
