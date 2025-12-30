package com.visionfocus.di.modules

import com.visionfocus.recognition.repository.RecognitionRepository
import com.visionfocus.recognition.repository.RecognitionRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for recognition components (Story 2.1)
 * 
 * Provides dependency injection bindings for:
 * - RecognitionRepository interface → RecognitionRepositoryImpl
 * - TFLiteInferenceEngine (already @Singleton)
 * - CameraManager (already @Singleton)
 * - ObjectRecognitionService (already @Singleton)
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RecognitionModule {
    
    @Binds
    @Singleton
    abstract fun bindRecognitionRepository(
        impl: RecognitionRepositoryImpl
    ): RecognitionRepository
}
