package com.visionfocus.di

import com.visionfocus.navigation.manager.NavigationManager
import com.visionfocus.navigation.manager.NavigationManagerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Module for binding NavigationManager interface to implementation.
 * 
 * Story 7.2 Code Review Fix: Separated from NavigationModule to avoid
 * potential multi-binding conflicts with NavigationRepository.
 * 
 * Following single-responsibility principle: one module per concern.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class NavigationManagerModule {
    
    /**
     * Binds NavigationManager interface to stub implementation.
     * 
     * Story 7.2: Stub implementation for saved locations navigation.
     * Epic 6: Full implementation with Google Maps Directions API.
     */
    @Binds
    @Singleton
    abstract fun bindNavigationManager(
        impl: NavigationManagerImpl
    ): NavigationManager
}
