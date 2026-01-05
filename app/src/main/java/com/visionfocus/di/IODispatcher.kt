package com.visionfocus.di

import javax.inject.Qualifier

/**
 * Qualifier annotation for IO Dispatcher injection.
 * 
 * Used to inject Dispatchers.IO for database operations and file I/O.
 * Allows mocking dispatcher in unit tests with TestCoroutineDispatcher.
 * 
 * Story 7.1 Code Review Fix: Added for SavedLocationRepositoryImpl testability.
 * Follows pattern established in other repositories (RecognitionHistoryRepositoryImpl).
 * 
 * Usage:
 * ```kotlin
 * class MyRepository @Inject constructor(
 *     @IODispatcher private val ioDispatcher: CoroutineDispatcher
 * ) {
 *     suspend fun doWork() = withContext(ioDispatcher) {
 *         // Database or file operations
 *     }
 * }
 * ```
 */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class IODispatcher
