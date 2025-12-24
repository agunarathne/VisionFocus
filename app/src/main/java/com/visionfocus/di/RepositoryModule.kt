package com.visionfocus.di

import com.visionfocus.data.repository.SampleRepository
import com.visionfocus.data.repository.SampleRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Module for binding repository interfaces to implementations.
 * 
 * @Binds is more efficient than @Provides for interface bindings
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindSampleRepository(
        sampleRepositoryImpl: SampleRepositoryImpl
    ): SampleRepository
}
