package com.visionfocus.di

import android.content.Context
import android.os.Vibrator
import com.visionfocus.accessibility.haptic.HapticFeedbackManager
import com.visionfocus.data.repository.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing haptic feedback dependencies (Story 5.4).
 * 
 * Provides HapticFeedbackManager as singleton with required dependencies.
 * 
 * @see com.visionfocus.accessibility.haptic.HapticFeedbackManager
 */
@Module
@InstallIn(SingletonComponent::class)
object HapticModule {
    
    /**
     * Provides HapticFeedbackManager singleton.
     * 
     * Story 5.4: Unified haptic feedback manager with intensity control
     * 
     * @param context Application context for Vibrator system service
     * @param settingsRepository Repository for haptic intensity preferences
     * @return Singleton HapticFeedbackManager instance
     */
    @Provides
    @Singleton
    fun provideHapticFeedbackManager(
        @ApplicationContext context: Context,
        settingsRepository: SettingsRepository
    ): HapticFeedbackManager {
        return HapticFeedbackManager(context, settingsRepository)
    }
}
