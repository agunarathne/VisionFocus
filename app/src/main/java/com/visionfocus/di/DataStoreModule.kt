package com.visionfocus.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing DataStore dependency.
 * 
 * DataStore is created using the preferencesDataStore delegate pattern,
 * which ensures a single instance per context.
 */

// Extension property for Context - creates DataStore singleton
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "visionfocus_preferences")

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {
    
    /**
     * Provides singleton DataStore<Preferences> instance.
     * 
     * DataStore is thread-safe and handles concurrent access internally.
     * All reads/writes are async via Kotlin Flow and suspend functions.
     */
    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }
}
