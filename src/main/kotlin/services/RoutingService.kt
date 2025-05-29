package services

import strategies.RoutingStrategy
import domain.entities.Customer
import domain.entities.Distributor
import domain.entities.Route
import factories.RoutingStrategyFactory
import repositories.SolutionRepository

class RoutingService(
    private var strategy: RoutingStrategy,
    private val solutionRepository: SolutionRepository = SolutionRepository.getInstance()
) {
    fun setStrategy(strategyName: String) {
        this.strategy = RoutingStrategyFactory.create(strategyName)
    }
    
    fun calculateRoutes(distributor: Distributor, customers: List<Customer>, strategyName: String): List<Route> {
        if (customers.isEmpty()) {
            throw IllegalArgumentException("Customer list cannot be empty")
        }
        
        // Use the strategy to solve the routing problem
        val routes = strategy.solve(distributor, customers)
        
        // Calculate complete paths for each route
        routes.forEach { it.calculatePath(distributor) }
        
        // Save the solution in the database
        solutionRepository.saveSolution(strategyName, distributor, routes)
        
        return routes
    }
}