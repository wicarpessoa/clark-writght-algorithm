import domain.entities.Customer
import domain.entities.Position
import domain.entities.Route
import kotlinx.serialization.*
import kotlinx.serialization.json.*

@Serializable
data class CustomerInput(val id: Int, val x: Double, val y: Double)

fun parseCustomers(json: String): List<Customer> {
    val inputList = Json.decodeFromString<List<CustomerInput>>(json)
    return inputList.map { Customer(it.id, Position(it.x, it.y)) }
}

fun formatRoutes(routes: List<Route>): String {
    return Json.encodeToString(routes.map { route ->
        route.customers.map { it.id }
    })
}