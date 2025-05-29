package controller

import application.Registry
import services.RoutingService
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import domain.entities.Customer
import domain.entities.Distributor
import domain.entities.Position
import domain.entities.Route
import repositories.SolutionRepository
import services.CustomerService
import services.DistributorService
import utils.JsonUtil
import utils.StrategyRequest
import kotlinx.serialization.Serializable

@Serializable
data class RouteResponse(
    val route: List<PointCoords>,
    val routeData: RouteData
)

@Serializable
data class PointCoords(
    val lat: Double,
    val lng: Double
)

@Serializable
data class RouteData(
    val totalDistance: Double,
    val totalTime: Double,
    val strategy: String,
    val pointData: List<PointData>
)

@Serializable
data class PointData(
    val id: Int,
    val value: Double
)

class SolveController : HttpHandler {
    private var registry = Registry.getInstance()
    private var routingService = registry.inject("routingService") as RoutingService
    private var customerService = registry.inject("customerService") as CustomerService
    private var distributorService = registry.inject("distributorService") as DistributorService
    private val solutionRepository = SolutionRepository.getInstance()

    private fun handleSolveRequest(exchange: HttpExchange) {
        val body = exchange.requestBody.reader().readText()
        val request = JsonUtil.fromJson<StrategyRequest>(body)
        
        // Determinar estratégia a ser usada
        val strategyName = request.strategy ?: request.strategyName ?: "clarkwright"
        
        // Processar centro de distribuição
        val distributor = if (request.distributionCenter != null) {
            // Criar distribuidor com coordenadas do novo formato
            val position = Position(
                request.distributionCenter.lat, 
                request.distributionCenter.lng
            )
            distributorService.create(Distributor(position))
        } else if (request.distributor != null) {
            // Usar formato antigo se disponível
            distributorService.create(Distributor(request.distributor.position))
        } else {
            // Usar o distribuidor existente se não for fornecido
            try {
                distributorService.get()
            } catch (e: Exception) {
                respond(exchange, 400, """{"error": "Distributor not found and not provided in request"}""")
                return
            }
        }
        
        // Processar pontos de entrega/clientes
        val customers = if (request.deliveryPoints != null && request.deliveryPoints.isNotEmpty()) {
            // Criar clientes a partir dos pontos de entrega do novo formato
            request.deliveryPoints.mapIndexed { index, point ->
                val position = Position(
                    point.lat, 
                    point.lng
                )
                customerService.create(position)
            }
        } else if (request.customers != null && request.customers.isNotEmpty()) {
            // Usar formato antigo se disponível
            request.customers.mapIndexed { index, customerInput ->
                if (customerInput.id != null) {
                    // Se tiver ID, tenta buscar cliente existente
                    customerService.getById(customerInput.id) ?: 
                    // Se não encontrar, cria um novo
                    customerService.create(customerInput.position)
                } else {
                    // Cria um novo cliente se não tiver ID
                    customerService.create(customerInput.position)
                }
            }
        } else {
            // Verifica se existem clientes cadastrados
            val existingCustomers = customerService.getAll()
            if (existingCustomers.isEmpty()) {
                respond(exchange, 400, """{"error": "No customers found and none provided in request"}""")
                return
            }
            existingCustomers
        }
        
        if (customers.isEmpty()) {
            respond(exchange, 400, """{"error": "No valid customers found"}""")
            return
        }
        
        // Calcular rotas
        routingService.setStrategy(strategyName)
        val resultRoutes = routingService.calculateRoutes(distributor, customers, strategyName)
        
        // Formatar resposta para o frontend
        if (resultRoutes.isNotEmpty()) {
            val route = resultRoutes[0]  // Considerando apenas a primeira rota por enquanto
            
            // Calcular tempo estimado (assumindo velocidade média de 5 km/h para caminhada a pé)
            val avgSpeedKmh = 5.0
            val totalTimeHours = route.totalDistance / avgSpeedKmh
            val totalTimeMinutes = totalTimeHours * 60
            
            // Extrair coordenadas para o frontend
            val routeCoords = route.path?.map { pos ->
                PointCoords(pos.x, pos.y)
            } ?: emptyList()
            
            // Gerar dados fictícios para cada ponto para visualização no gráfico
            // Em uma aplicação real, esses valores seriam baseados em dados reais
            val pointDataList = customers.mapIndexed { index, customer ->
                // Gerar um valor que representa algo como "economia" ou "prioridade"
                val value = 50.0 + (index * 10.0) % 50.0 // Valor entre 50 e 100
                PointData(index, value)
            }
            
            // Criar objeto de resposta
            val routeData = RouteData(
                totalDistance = route.totalDistance,
                totalTime = totalTimeMinutes,
                strategy = strategyName,
                pointData = pointDataList
            )
            
            val response = RouteResponse(
                route = routeCoords,
                routeData = routeData
            )
            
            respond(exchange, 200, JsonUtil.toJson(response))
        } else {
            respond(exchange, 404, """{"error": "No route found"}""")
        }
    }
    
    private fun handleGetSolutions(exchange: HttpExchange) {
        val solutions = solutionRepository.getAllSolutions()
        val response = JsonUtil.toJson(solutions)
        respond(exchange, 200, response)
    }

    override fun handle(exchange: HttpExchange) {
        try {
            when (exchange.requestMethod) {
                "POST" -> handleSolveRequest(exchange)
                "GET" -> handleGetSolutions(exchange)
                "OPTIONS" -> handleCorsOptions(exchange)
                else -> {
                    exchange.sendResponseHeaders(405, -1)
                }
            }
        } catch(e: Exception) {
            e.printStackTrace()
            respond(exchange, 500, """{"error": "${e.message}"}""")
        }
    }
    
    // Tratamento para CORS quando o frontend estiver em um domínio diferente
    private fun handleCorsOptions(exchange: HttpExchange) {
        setCorsHeaders(exchange)
        exchange.sendResponseHeaders(204, -1)
    }
    
    private fun setCorsHeaders(exchange: HttpExchange) {
        exchange.responseHeaders.add("Access-Control-Allow-Origin", "*")
        exchange.responseHeaders.add("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
        exchange.responseHeaders.add("Access-Control-Allow-Headers", "Content-Type")
    }

    private fun respond(exchange: HttpExchange, statusCode: Int, response: String) {
        setCorsHeaders(exchange)
        exchange.responseHeaders.add("Content-Type", "application/json")
        exchange.sendResponseHeaders(statusCode, response.toByteArray().size.toLong())
        exchange.responseBody.use { os -> os.write(response.toByteArray()) }
        exchange.close()
    }
}
