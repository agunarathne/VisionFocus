package com.visionfocus.di.modules

import com.visionfocus.data.local.dao.OfflineMapDao
import com.visionfocus.data.repository.OfflineMapRepository
import com.visionfocus.data.repository.OfflineMapRepositoryImpl
import com.visionfocus.maps.MapboxOfflineManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Story 7.4: Hilt module for offline map dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class OfflineMapModule {
    
    @Binds
    @Singleton
    abstract fun bindOfflineMapRepository(
        impl: OfflineMapRepositoryImpl
    ): OfflineMapRepository
}
