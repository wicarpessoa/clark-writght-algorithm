package domain.entities
import kotlinx.serialization.Serializable

@Serializable
data class Distributor(
    val position: Position,
    val id: Int? = null
)
