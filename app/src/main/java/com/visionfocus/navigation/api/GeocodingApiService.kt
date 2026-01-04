package com.visionfocus.navigation.api

import android.content.Context
import com.google.gson.GsonBuilder
import com.visionfocus.BuildConfig
import com.visionfocus.navigation.models.LatLng
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import timber.log.Timber
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for Google Maps Geocoding API integration.
 * 
 * Converts destination names/addresses to geographic coordinates (latitude/longitude).
 * Required for Story 6.2 destination validation with real geocoding.
 * 
 * @property context Application context for resources
 */
@Singleton
class GeocodingApiService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val TAG = "GeocodingApiService"
        private const val BASE_URL = "https://maps.googleapis.com/maps/api/"
        private const val CONNECT_TIMEOUT = 10L  // seconds
        private const val READ_TIMEOUT = 15L     // seconds (shorter than Directions)
    }
    
    private val apiKey: String by lazy {
        BuildConfig.MAPS_API_KEY
    }
    
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(createOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create(createGson()))
            .build()
    }
    
    private val api: GeocodingApi by lazy {
        retrofit.create(GeocodingApi::class.java)
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
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .build()
    }
    
    private fun createGson() = GsonBuilder()
        .setLenient()
        .create()
    
    /**
     * Geocode a destination query to coordinates.
     * 
     * @param query Destination name/address (e.g., "Temple of the Tooth, Kandy")
     * @return Result with coordinates or error
     */
    suspend fun geocode(query: String): Result<LatLng> {
        return withContext(Dispatchers.IO) {
            try {
                Timber.tag(TAG).d("Geocoding: $query")
                
                val response: Response<GeocodingResponse> = api.geocode(
                    address = query,
                    key = apiKey
                )
                
                if (!response.isSuccessful) {
                    Timber.tag(TAG).e("Geocoding API error: HTTP ${response.code()}")
                    return@withContext Result.failure(
                        parseHttpError(response.code(), response.errorBody()?.string())
                    )
                }
                
                val body = response.body()
                if (body == null) {
                    Timber.tag(TAG).e("Empty geocoding response")
                    return@withContext Result.failure(
                        DirectionsError.ApiError("Empty geocoding response")
                    )
                }
                
                // Parse response
                when (body.status) {
                    "OK" -> {
                        if (body.results.isEmpty()) {
                            Timber.tag(TAG).e("No geocoding results")
                            return@withContext Result.failure(
                                DirectionsError.ApiError("No location found for: $query")
                            )
                        }
                        
                        val location = body.results[0].geometry.location
                        val coordinates = LatLng(location.lat, location.lng)
                        Timber.tag(TAG).d("Geocoded to: ${coordinates.latitude}, ${coordinates.longitude}")
                        Result.success(coordinates)
                    }
                    "ZERO_RESULTS" -> {
                        Timber.tag(TAG).e("No results for: $query")
                        Result.failure(
                            DirectionsError.ApiError("Location not found: $query")
                        )
                    }
                    "REQUEST_DENIED" -> {
                        Timber.tag(TAG).e("Geocoding API key denied")
                        Result.failure(
                            DirectionsError.InvalidApiKey("Invalid API key for Geocoding API")
                        )
                    }
                    "INVALID_REQUEST" -> {
                        Timber.tag(TAG).e("Invalid geocoding request")
                        Result.failure(
                            DirectionsError.InvalidRequest("Invalid destination: $query")
                        )
                    }
                    else -> {
                        Timber.tag(TAG).e("Geocoding error: ${body.status}")
                        Result.failure(
                            DirectionsError.ApiError("Geocoding error: ${body.status}")
                        )
                    }
                }
                
            } catch (e: IOException) {
                Timber.tag(TAG).e(e, "Network error")
                Result.failure(DirectionsError.NetworkUnavailable("Cannot reach Geocoding API. Check internet connection."))
            } catch (e: SocketTimeoutException) {
                Timber.tag(TAG).e(e, "Request timeout")
                Result.failure(DirectionsError.Timeout("Geocoding request timed out. Please try again."))
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Unexpected geocoding error")
                Result.failure(DirectionsError.Unknown("Geocoding error: ${e.message}"))
            }
        }
    }
    
    private fun parseHttpError(code: Int, errorBody: String?): DirectionsError {
        return when (code) {
            403 -> DirectionsError.InvalidApiKey("Invalid Google Maps API key for Geocoding")
            429 -> DirectionsError.QuotaExceeded("Geocoding API quota exceeded")
            400 -> DirectionsError.InvalidRequest("Invalid geocoding request")
            else -> DirectionsError.ApiError("Geocoding API error (HTTP $code): $errorBody")
        }
    }
}

/**
 * Retrofit interface for Google Maps Geocoding API.
 */
interface GeocodingApi {
    @GET("geocode/json")
    suspend fun geocode(
        @Query("address") address: String,
        @Query("key") key: String
    ): Response<GeocodingResponse>
}

/**
 * Geocoding API response models.
 */
data class GeocodingResponse(
    val results: List<GeocodingResult>,
    val status: String
)

data class GeocodingResult(
    val formatted_address: String,
    val geometry: Geometry,
    val place_id: String
)

data class Geometry(
    val location: Location
)

data class Location(
    val lat: Double,
    val lng: Double
)
