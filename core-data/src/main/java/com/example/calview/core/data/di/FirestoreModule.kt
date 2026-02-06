package com.example.calview.core.data.di

import com.example.calview.core.data.repository.FirestoreRepository
import com.example.calview.core.data.repository.FirestoreRepositoryImpl
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirestoreModule {
    
    // private const val DATABASE_ID = "calview"
    
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        // Use the default database instance
        return Firebase.firestore
    }
    
    @Provides
    @Singleton
    fun provideFirestoreRepository(
        firestore: FirebaseFirestore,
        storageRepository: com.example.calview.core.data.repository.StorageRepository
    ): FirestoreRepository {
        return FirestoreRepositoryImpl(firestore, storageRepository)
    }
}
