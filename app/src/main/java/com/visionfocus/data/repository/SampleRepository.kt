package com.visionfocus.data.repository

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Sample repository interface demonstrating Clean Architecture data layer.
 * 
 * Repository interfaces define contracts for data access without exposing
 * implementation details.
 */
interface SampleRepository {
    fun getSampleData(): String
}

/**
 * Sample repository implementation.
 * 
 * @Inject constructor tells Hilt how to provide instances of this class
 * @Singleton ensures only one instance exists throughout app lifecycle
 */
@Singleton
class SampleRepositoryImpl @Inject constructor() : SampleRepository {
    
    override fun getSampleData(): String {
        return "Hilt dependency injection working!"
    }
}
