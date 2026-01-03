package com.visionfocus.navigation.api

import android.content.Context
import com.google.gson.GsonBuilder
import com.visionfocus.BuildConfig
import com.visionfocus.navigation.consent.NetworkConsentManager
import com.visionfocus.navigation.models.LatLng
import com.visionfocus.navigation.models.NavigationRoute
import com.visionfocus.navigation.models.TravelMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for Google Maps Directions API integration.
 * 
 * Story 6.2: Requests turn-by-turn directions via Retrofit HTTP client.
 * Handles network consent, API key security, error handling, and response parsing.
 * 
 * @property context Application context for resources
 * @property networkConsentManager Consent checker before API calls
 */
@Singleton
class DirectionsApiService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val networkConsentManager: NetworkConsentManager
) {
    
    companion object {
        private const val TAG = "DirectionsApiService"
        private const val BASE_URL = "https://maps.googleapis.com/maps/api/directions/"
        private const val CONNECT_TIMEOUT = 10L  // seconds
        private const val READ_TIMEOUT = 30L     // seconds
    }
    
    private val apiKey: String by lazy {
        // Read from BuildConfig (generated from local.properties)
        BuildConfig.MAPS_API_KEY
    }
    
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(createOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create(createGson()))
            .build()
    }
    
    private val api: DirectionsApi by lazy {
        retrofit.create(DirectionsApi::class.java)
    }
    
    private fun createOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        
        return OkHttpClient.Builder()
            // CODE REVIEW FIX (Issue #6): Timeout rationale
            // 10s connect: Accounts for slow mobile networks (3G/4G) and cellular latency
            // 30s read: Google Directions API can take 15-20s for complex multi-leg routes
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .build()
    }
    
    private fun createGson() = GsonBuilder()
        .setLenient()
        .create()
    
    /**
     * Requests turn-by-turn directions from Google Maps Directions API.
     * 
     * Requires network consent from user before making API call.
     * Returns Result.success with NavigationRoute or Result.failure with DirectionsError.
     * 
     * @param origin Starting location coordinates
     * @param destination Ending location coordinates
     * @param travelMode Walking (default), Driving, Bicycling, Transit
     * @return Result<NavigationRoute> with route data or error
     */
    suspend fun getDirections(
        origin: LatLng,
        destination: LatLng,
        travelMode: TravelMode = TravelMode.WALKING
    ): Result<NavigationRoute> {
        return withContext(Dispatchers.IO) {
            try {
                // Check network consent before API call
                if (!networkConsentManager.hasConsent()) {
                    Timber.tag(TAG).w("Network consent required")
                    return@withContext Result.failure(
                        DirectionsError.ConsentRequired("Network consent required for live directions")
                    )
                }
                
                Timber.tag(TAG).d("Requesting directions: $origin → $destination")
                
                // Make API request
                val response = api.getDirections(
                    origin = origin.toString(),
                    destination = destination.toString(),
                    mode = travelMode.apiValue,
                    key = apiKey
                )
                
                // Check HTTP status
                if (!response.isSuccessful) {
                    Timber.tag(TAG).e("API error: HTTP ${response.code()}")
                    return@withContext Result.failure(
                        parseHttpError(response.code(), response.errorBody()?.string())
                    )
                }
                
                // Parse response
                val body = response.body()
                if (body == null) {
                    Timber.tag(TAG).e("Empty response from Directions API")
                    return@withContext Result.failure(
                        DirectionsError.ApiError("Empty response from Directions API")
                    )
                }
                
                // Convert to domain model
                val route = DirectionsResponseParser.parse(body, origin, destination)
                
                Timber.tag(TAG).d("Route received: ${route.steps.size} steps, ${route.totalDistance}m, ${route.totalDuration}s")
                
                Result.success(route)
                
            } catch (e: IOException) {
                Timber.tag(TAG).e(e, "Network error")
                Result.failure(DirectionsError.NetworkUnavailable("Cannot reach Google Maps API. Check internet connection."))
            } catch (e: SocketTimeoutException) {
                Timber.tag(TAG).e(e, "Request timeout")
                Result.failure(DirectionsError.Timeout("Directions request timed out. Please try again."))
            } catch (e: DirectionsError) {
                // Re-throw DirectionsError from parser
                Timber.tag(TAG).e(e, "Directions API error")
                Result.failure(e)
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Unexpected error")
                Result.failure(DirectionsError.Unknown("Unexpected error: ${e.message}"))
            }
        }
    }
    
    private fun parseHttpError(code: Int, errorBody: String?): DirectionsError {
        return when (code) {
            403 -> DirectionsError.InvalidApiKey("Invalid Google Maps API key. Check configuration.")
            429 -> DirectionsError.QuotaExceeded("API quota exceeded. Try again later.")
            400 -> DirectionsError.InvalidRequest("Invalid origin or destination coordinates.")
            else -> DirectionsError.ApiError("API error (HTTP $code): $errorBody")
        }
    }
}
