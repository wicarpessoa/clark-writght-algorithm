package strategies

import domain.entities.Customer
import domain.entities.Distributor
import domain.entities.Route

interface RoutingStrategy {
    fun solve(distributor: Distributor, customers: List<Customer>): List<Route>
}