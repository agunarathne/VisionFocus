package com.visionfocus.di.modules

import android.content.Context
import com.visionfocus.tts.engine.TTSManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt dependency injection module for TTS components
 * 
 * Story 2.2: Text-to-Speech engine and formatter bindings
 * 
 * Provides:
 * - TTSManager: Android TextToSpeech service wrapper
 * - TTSPhraseFormatter: Confidence-aware announcement formatting
 */
@Module
@InstallIn(SingletonComponent::class)
object TTSModule {
    
    /**
     * Provide TTSManager singleton
     * Initialized on app startup, available throughout app lifecycle
     * 
     * @param context Application context for TextToSpeech initialization
     * @return TTSManager singleton instance
     */
    @Provides
    @Singleton
    fun provideTTSManager(
        @ApplicationContext context: Context
    ): TTSManager {
        return TTSManager(context).apply {
            initialize()  // Initialize TTS on app startup
        }
    }
}
