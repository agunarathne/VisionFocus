package com.visionfocus.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing application-level dependencies.
 * 
 * @Module indicates this is a Hilt module
 * @InstallIn(SingletonComponent::class) means dependencies are available throughout the app lifecycle
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    /**
     * Provides the application Context.
     * 
     * @ApplicationContext ensures we get the Application context, not Activity context
     * (prevents memory leaks)
     */
    @Provides
    @Singleton
    fun provideApplicationContext(@ApplicationContext context: Context): Context {
        return context
    }
    
    // Future app-level dependencies will be added here:
    // - TFLite model loader (Epic 2)
    // - Room database (Story 1.4)
    // - DataStore preferences (Story 1.3)
    // - TTS engine (Epic 2)
    // - Audio priority manager (Epic 8)
}
