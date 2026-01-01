package com.visionfocus.di.modules

import android.content.Context
import com.visionfocus.accessibility.haptic.HapticFeedbackManager
import com.visionfocus.tts.engine.TTSManager
import com.visionfocus.voice.commands.navigation.BackCommand
import com.visionfocus.voice.commands.navigation.HistoryCommand
import com.visionfocus.voice.commands.navigation.HomeCommand
import com.visionfocus.voice.commands.navigation.NavigateCommand
import com.visionfocus.voice.commands.navigation.WhereAmICommand
import com.visionfocus.voice.commands.recognition.RecognizeCommand
import com.visionfocus.voice.commands.recognition.SaveLocationCommand
import com.visionfocus.voice.commands.recognition.WhatDoISeeCommand
import com.visionfocus.voice.commands.settings.DecreaseSpeedCommand
import com.visionfocus.voice.commands.settings.HighContrastOffCommand
import com.visionfocus.voice.commands.settings.HighContrastOnCommand
import com.visionfocus.voice.commands.settings.IncreaseSpeedCommand
import com.visionfocus.voice.commands.settings.SettingsCommand
import com.visionfocus.voice.commands.settings.VerbosityBriefCommand
import com.visionfocus.voice.commands.settings.VerbosityDetailedCommand
import com.visionfocus.voice.commands.settings.VerbosityStandardCommand
import com.visionfocus.voice.commands.utility.CancelCommand
import com.visionfocus.voice.commands.utility.HelpCommand
import com.visionfocus.voice.commands.utility.RepeatCommand
import com.visionfocus.voice.processor.VoiceCommandProcessor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for voice command system (Story 3.2)
 * 
 * Provides dependency injection bindings for:
 * - VoiceCommandProcessor (central dispatcher)
 * - All 15 core VoiceCommand implementations
 * 
 * Automatic command registration:
 * The @Provides method registers all commands with the processor
 * upon creation, ensuring they're available for voice recognition.
 * 
 * @see VoiceCommandProcessor
 * @see VoiceCommand
 */
@Module
@InstallIn(SingletonComponent::class)
object VoiceCommandModule {
    
    /**
     * Provide VoiceCommandProcessor with all commands registered.
     * Story 3.2 Task 1: Command registry initialization
     * 
     * Creates processor and registers all 15 commands.
     * 
     * @param context Application context
     * @param ttsManager TTS engine
     * @param hapticFeedbackManager Haptic feedback
     * @param recognizeCommand Recognition commands (Story 3.2 Task 2.1)
     * @param navigateCommand Navigation commands (Story 3.2 Task 2.2)
     * @param repeatCommand Utility commands (Story 3.2 Task 2.3)
     * @param cancelCommand Utility commands (Story 3.2 Task 2.4)
     * @param settingsCommand Settings commands (Story 3.2 Task 2.5)
     * @param saveLocationCommand Location commands (Story 3.2 Task 2.6)
     * @param highContrastOnCommand Settings commands (Story 3.2 Task 2.7)
     * @param highContrastOffCommand Settings commands (Story 3.2 Task 2.7)
     * @param increaseSpeedCommand Settings commands (Story 3.2 Task 2.8)
     * @param decreaseSpeedCommand Settings commands (Story 3.2 Task 2.9)
     * @param historyCommand Recognition commands (Story 3.2 Task 2.10)
     * @param helpCommand Utility commands (Story 3.2 Task 2.11)
     * @param backCommand Navigation commands (Story 3.2 Task 2.12)
     * @param homeCommand Navigation commands (Story 3.2 Task 2.13)
     * @param whereAmICommand Navigation commands (Story 3.2 Task 2.14)
     * @param whatDoISeeCommand Recognition commands (Story 3.2 Task 2.15)
     * @param verbosityBriefCommand Settings commands (Story 4.1 Task 7)
     * @param verbosityStandardCommand Settings commands (Story 4.1 Task 7)
     * @param verbosityDetailedCommand Settings commands (Story 4.1 Task 7)
     * @return Fully configured VoiceCommandProcessor
     */
    @Provides
    @Singleton
    fun provideVoiceCommandProcessor(
        @ApplicationContext context: Context,
        ttsManager: TTSManager,
        hapticFeedbackManager: HapticFeedbackManager,
        // Recognition commands
        recognizeCommand: RecognizeCommand,
        whatDoISeeCommand: WhatDoISeeCommand,
        historyCommand: HistoryCommand,
        // Navigation commands
        navigateCommand: NavigateCommand,
        whereAmICommand: WhereAmICommand,
        backCommand: BackCommand,
        homeCommand: HomeCommand,
        // Settings commands
        settingsCommand: SettingsCommand,
        highContrastOnCommand: HighContrastOnCommand,
        highContrastOffCommand: HighContrastOffCommand,
        increaseSpeedCommand: IncreaseSpeedCommand,
        decreaseSpeedCommand: DecreaseSpeedCommand,
        verbosityBriefCommand: VerbosityBriefCommand,
        verbosityStandardCommand: VerbosityStandardCommand,
        verbosityDetailedCommand: VerbosityDetailedCommand,
        // Utility commands
        repeatCommand: RepeatCommand,
        cancelCommand: CancelCommand,
        helpCommand: HelpCommand,
        // Location commands
        saveLocationCommand: SaveLocationCommand
    ): VoiceCommandProcessor {
        // Create processor
        val processor = VoiceCommandProcessor(context, ttsManager, hapticFeedbackManager)
        
        // Register all commands with the processor
        processor.registerCommand(recognizeCommand)
        processor.registerCommand(navigateCommand)
        processor.registerCommand(repeatCommand)
        processor.registerCommand(cancelCommand)
        processor.registerCommand(settingsCommand)
        processor.registerCommand(saveLocationCommand)
        processor.registerCommand(highContrastOnCommand)
        processor.registerCommand(highContrastOffCommand)
        processor.registerCommand(increaseSpeedCommand)
        processor.registerCommand(decreaseSpeedCommand)
        processor.registerCommand(historyCommand)
        processor.registerCommand(helpCommand)
        processor.registerCommand(backCommand)
        processor.registerCommand(homeCommand)
        processor.registerCommand(whereAmICommand)
        processor.registerCommand(whatDoISeeCommand)
        processor.registerCommand(verbosityBriefCommand)
        processor.registerCommand(verbosityStandardCommand)
        processor.registerCommand(verbosityDetailedCommand)
        
        return processor
    }
}
