package strategies

import domain.entities.Customer
import domain.entities.Distributor
import domain.entities.Route

class NearestNeighbor: RoutingStrategy {
    override fun solve(distributor: Distributor, customers: List<Customer>): List<Route> {
        val routes = mutableListOf<Route>()
        return routes
    }
}