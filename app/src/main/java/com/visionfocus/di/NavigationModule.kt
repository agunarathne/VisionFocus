package com.visionfocus.di

import com.visionfocus.navigation.repository.NavigationRepository
import com.visionfocus.navigation.repository.NavigationRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Module for binding NavigationRepository interface to implementation.
 * 
 * Story 7.2 Code Review Fix: NavigationManager binding moved to separate
 * NavigationManagerModule to follow single-responsibility principle.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class NavigationModule {

    @Binds
    @Singleton
    abstract fun bindNavigationRepository(
        impl: NavigationRepositoryImpl
    ): NavigationRepository
}
