package domain.entities

import kotlinx.serialization.Serializable
import java.time.Instant
import serializers.InstantSerializer
import kotlinx.serialization.Contextual

@Serializable
data class Solution(
    val id: Int,
    val strategyName: String,
    val distributorId: Int,
    val totalDistance: Double,
    val routes: List<RouteWithPoints> = emptyList(),
    @Serializable(with = InstantSerializer::class)
    val createdAt: Instant
)

@Serializable
data class RouteWithPoints(
    val id: Int,
    val solutionId: Int,
    val sequence: Int,
    val distance: Double,
    val points: List<RoutePoint>,
    @Serializable(with = InstantSerializer::class)
    val createdAt: Instant
)

@Serializable
data class RoutePoint(
    val id: Int,
    val routeId: Int,
    val customerId: Int?,
    val distributorId: Int?,
    val sequenceNumber: Int
) 