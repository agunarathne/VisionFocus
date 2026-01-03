package com.visionfocus.navigation.api

import com.visionfocus.navigation.models.LatLng
import com.visionfocus.navigation.models.Maneuver
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for DirectionsResponseParser.
 * 
 * Story 6.2: Validates JSON parsing, HTML stripping, maneuver mapping.
 */
class DirectionsResponseParserTest {
    
    @Test
    fun `parse valid response with single leg and multiple steps`() {
        // Given
        val response = DirectionsResponseDto(
            status = "OK",
            routes = listOf(
                RouteDto(
                    summary = "via Main St",
                    legs = listOf(
                        LegDto(
                            distance = DistanceDto(value = 1234, text = "1.2 km"),
                            duration = DurationDto(value = 900, text = "15 mins"),
                            start_address = "123 Start St",
                            end_address = "456 End Ave",
                            steps = listOf(
                                StepDto(
                                    html_instructions = "Head <b>north</b> on <b>Main St</b>",
                                    distance = DistanceDto(value = 200, text = "200 m"),
                                    duration = DurationDto(value = 120, text = "2 mins"),
                                    maneuver = "turn-left",
                                    start_location = LocationDto(lat = 40.7128, lng = -74.0060),
                                    end_location = LocationDto(lat = 40.7148, lng = -74.0060),
                                    polyline = PolylineDto("encodedPolyline")
                                )
                            )
                        )
                    ),
                    overview_polyline = PolylineDto("overviewPolyline")
                )
            )
        )
        
        val origin = LatLng(40.7128, -74.0060)
        val destination = LatLng(40.7148, -74.0060)
        
        // When
        val route = DirectionsResponseParser.parse(response, origin, destination)
        
        // Then
        assertEquals(1, route.steps.size)
        assertEquals(1234, route.totalDistance)
        assertEquals(900, route.totalDuration)
        assertEquals("via Main St", route.summary)
        assertEquals("overviewPolyline", route.polyline)
        
        // Verify step details
        val step = route.steps[0]
        assertEquals("Head north on Main St", step.instruction)  // HTML stripped
        assertEquals(200, step.distance)
        assertEquals(120, step.duration)
        assertEquals(Maneuver.TURN_LEFT, step.maneuver)
        assertEquals(40.7128, step.startLocation.latitude, 0.0001)
        assertEquals(-74.0060, step.startLocation.longitude, 0.0001)
    }
    
    @Test
    fun `stripHtml removes all HTML tags correctly`() {
        // Given
        val htmlInstruction = "Turn <b>left</b> onto <b>Main Street</b>"
        val response = createMinimalResponse(htmlInstruction, "turn-left")
        
        // When
        val route = DirectionsResponseParser.parse(response, LatLng(0.0, 0.0), LatLng(0.0, 0.0))
        
        // Then
        assertEquals("Turn left onto Main Street", route.steps[0].instruction)
    }
    
    @Test
    fun `stripHtml handles HTML entities correctly`() {
        // Given
        val htmlInstruction = "Turn &amp; proceed to Main&nbsp;St"
        val response = createMinimalResponse(htmlInstruction, null)
        
        // When
        val route = DirectionsResponseParser.parse(response, LatLng(0.0, 0.0), LatLng(0.0, 0.0))
        
        // Then
        assertEquals("Turn & proceed to Main St", route.steps[0].instruction)
    }
    
    @Test
    fun `parse handles all maneuver types correctly`() {
        val maneuverTests = mapOf(
            "turn-left" to Maneuver.TURN_LEFT,
            "turn-right" to Maneuver.TURN_RIGHT,
            "turn-slight-left" to Maneuver.TURN_SLIGHT_LEFT,
            "turn-slight-right" to Maneuver.TURN_SLIGHT_RIGHT,
            "turn-sharp-left" to Maneuver.TURN_SHARP_LEFT,
            "turn-sharp-right" to Maneuver.TURN_SHARP_RIGHT,
            "straight" to Maneuver.STRAIGHT,
            "ramp-left" to Maneuver.RAMP_LEFT,
            "ramp-right" to Maneuver.RAMP_RIGHT,
            "merge" to Maneuver.MERGE,
            "fork-left" to Maneuver.FORK_LEFT,
            "fork-right" to Maneuver.FORK_RIGHT,
            "roundabout-left" to Maneuver.ROUNDABOUT_LEFT,
            "roundabout-right" to Maneuver.ROUNDABOUT_RIGHT,
            "uturn-left" to Maneuver.UTURN_LEFT,
            "uturn-right" to Maneuver.UTURN_RIGHT,
            null to Maneuver.UNKNOWN,
            "invalid-maneuver" to Maneuver.UNKNOWN
        )
        
        maneuverTests.forEach { (apiValue, expectedManeuver) ->
            val response = createMinimalResponse("Turn here", apiValue)
            val route = DirectionsResponseParser.parse(response, LatLng(0.0, 0.0), LatLng(0.0, 0.0))
            
            assertEquals(
                "Failed for maneuver: $apiValue",
                expectedManeuver,
                route.steps[0].maneuver
            )
        }
    }
    
    @Test
    fun `parse handles multiple legs correctly`() {
        // Given
        val response = DirectionsResponseDto(
            status = "OK",
            routes = listOf(
                RouteDto(
                    summary = "via Main St and Oak Ave",
                    legs = listOf(
                        createLeg(distance = 500, duration = 300, steps = 2),
                        createLeg(distance = 800, duration = 400, steps = 3)
                    ),
                    overview_polyline = PolylineDto("polyline")
                )
            )
        )
        
        // When
        val route = DirectionsResponseParser.parse(response, LatLng(0.0, 0.0), LatLng(0.0, 0.0))
        
        // Then
        assertEquals(5, route.steps.size)  // 2 + 3 steps
        assertEquals(1300, route.totalDistance)  // 500 + 800
        assertEquals(700, route.totalDuration)  // 300 + 400
    }
    
    @Test(expected = DirectionsError.ApiError::class)
    fun `parse throws error when status is not OK`() {
        // Given
        val response = DirectionsResponseDto(
            status = "ZERO_RESULTS",
            routes = emptyList()
        )
        
        // When / Then
        DirectionsResponseParser.parse(response, LatLng(0.0, 0.0), LatLng(0.0, 0.0))
    }
    
    @Test(expected = DirectionsError.ApiError::class)
    fun `parse throws error when routes array is empty`() {
        // Given
        val response = DirectionsResponseDto(
            status = "OK",
            routes = emptyList()
        )
        
        // When / Then
        DirectionsResponseParser.parse(response, LatLng(0.0, 0.0), LatLng(0.0, 0.0))
    }
    
    @Test
    fun `parse handles missing summary correctly`() {
        // Given
        val response = DirectionsResponseDto(
            status = "OK",
            routes = listOf(
                RouteDto(
                    summary = null,  // Missing summary
                    legs = listOf(createLeg(distance = 100, duration = 60, steps = 1)),
                    overview_polyline = PolylineDto("polyline")
                )
            )
        )
        
        // When
        val route = DirectionsResponseParser.parse(response, LatLng(0.0, 0.0), LatLng(0.0, 0.0))
        
        // Then
        assertEquals("Route to destination", route.summary)  // Default summary
    }
    
    // Helper functions
    
    private fun createMinimalResponse(instruction: String, maneuver: String?): DirectionsResponseDto {
        return DirectionsResponseDto(
            status = "OK",
            routes = listOf(
                RouteDto(
                    summary = "Test Route",
                    legs = listOf(
                        LegDto(
                            distance = DistanceDto(value = 100, text = "100 m"),
                            duration = DurationDto(value = 60, text = "1 min"),
                            start_address = "Start",
                            end_address = "End",
                            steps = listOf(
                                StepDto(
                                    html_instructions = instruction,
                                    distance = DistanceDto(value = 100, text = "100 m"),
                                    duration = DurationDto(value = 60, text = "1 min"),
                                    maneuver = maneuver,
                                    start_location = LocationDto(lat = 0.0, lng = 0.0),
                                    end_location = LocationDto(lat = 0.0, lng = 0.0),
                                    polyline = null
                                )
                            )
                        )
                    ),
                    overview_polyline = PolylineDto("polyline")
                )
            )
        )
    }
    
    private fun createLeg(distance: Int, duration: Int, steps: Int): LegDto {
        val stepList = (1..steps).map {
            StepDto(
                html_instructions = "Step $it",
                distance = DistanceDto(value = distance / steps, text = "${distance / steps} m"),
                duration = DurationDto(value = duration / steps, text = "${duration / steps} s"),
                maneuver = "straight",
                start_location = LocationDto(lat = 0.0, lng = 0.0),
                end_location = LocationDto(lat = 0.0, lng = 0.0),
                polyline = null
            )
        }
        
        return LegDto(
            distance = DistanceDto(value = distance, text = "$distance m"),
            duration = DurationDto(value = duration, text = "$duration s"),
            start_address = "Start",
            end_address = "End",
            steps = stepList
        )
    }
}
