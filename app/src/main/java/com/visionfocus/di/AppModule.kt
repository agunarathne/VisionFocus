package com.visionfocus.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
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
    
    /**
     * Provides IO Dispatcher for database and file operations.
     * 
     * Story 7.1 Code Review Fix: Added for testability in repositories.
     * Allows mocking with TestCoroutineDispatcher in unit tests.
     */
    @Provides
    @Singleton
    @IODispatcher
    fun provideIODispatcher(): CoroutineDispatcher = Dispatchers.IO
    
    // Future app-level dependencies will be added here:
    // - TFLite model loader (Epic 2)
    // - Room database (Story 1.4)
    // - DataStore preferences (Story 1.3)
    // - TTS engine (Epic 2)
    // - Audio priority manager (Epic 8)
}
