import application.Registry
import com.sun.net.httpserver.HttpServer
import config.DatabaseConfig
import controller.SolveController
import controller.StrategyController
import factories.RoutingStrategyFactory
import services.CustomerService
import services.DistributorService
import services.RoutingService
import java.net.InetSocketAddress

fun main() {
    // Initialize the database
    DatabaseConfig.init()
    
    // Set up services
    val routingService = RoutingService(RoutingStrategyFactory.create("clarkwright"))
    val customerService = CustomerService()
    val distributorService = DistributorService()
    
    // Register services in the application registry
    val registry = Registry.getInstance()
    registry.provide("routingService", routingService)
    registry.provide("customerService", customerService)
    registry.provide("distributorService", distributorService)

    // Create controllers
    val solveController = SolveController()
    val strategyController = StrategyController()

    // Set up HTTP server
    val server = HttpServer.create(InetSocketAddress(8080), 0)
    server.createContext("/solve", solveController)
    server.createContext("/strategies", strategyController)

    server.executor = null
    server.start()
    println("Server started on port 8080...")
    println("Available endpoints:")
    println("  - GET /strategies - List available strategies")
    println("  - POST /solve - Calculate routes using specified strategy")
    println("  - GET /solve - List saved solutions")
}
