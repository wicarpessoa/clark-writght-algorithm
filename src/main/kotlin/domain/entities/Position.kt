package domain.entities

import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.math.sin
import kotlin.math.cos
import kotlin.math.atan2
import kotlinx.serialization.Serializable

@Serializable
class Position(var x: Double, var y: Double) {
    // Latitude/Longitude para facilitar o uso com coordenadas geográficas
    val latitude: Double
        get() = x
    
    val longitude: Double
        get() = y
    
    fun calculateDistance(position: Position): Double {
        // Para coordenadas geográficas, usamos a fórmula de Haversine para calcular
        // a distância entre dois pontos na superfície da Terra
        val R = 6371.0 // Raio da Terra em km
        
        val lat1 = Math.toRadians(this.latitude)
        val lon1 = Math.toRadians(this.longitude)
        val lat2 = Math.toRadians(position.latitude)
        val lon2 = Math.toRadians(position.longitude)
        
        val dLat = lat2 - lat1
        val dLon = lon2 - lon1
        
        val a = sin(dLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        
        return R * c // Distância em km
    }

    override fun toString(): String {
        return "($x, $y)"
    }
}
