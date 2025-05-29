package utils

import domain.entities.Position
import kotlinx.serialization.Serializable

@Serializable
data class StrategyRequest(
    val strategy: String,
    val distributionCenter: DistributorInputCoords? = null,
    val deliveryPoints: List<PointCoords>? = null,
    // Manter compatibilidade com formato antigo
    val strategyName: String? = null,
    val distributor: DistributorInput? = null,
    val customers: List<CustomerInput>? = null
)

@Serializable
data class DistributorInputCoords(
    val lat: Double,
    val lng: Double
)

@Serializable
data class PointCoords(
    val lat: Double,
    val lng: Double
)

@Serializable
data class DistributorInput(
    val position: Position
)

@Serializable
data class CustomerInput(
    val id: Int? = null,
    val position: Position
)