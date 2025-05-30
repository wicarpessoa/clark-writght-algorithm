package controller

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import factories.RoutingStrategyFactory
import kotlinx.serialization.Serializable
import utils.JsonUtil

@Serializable
data class StrategyInfo(val name: String, val description: String)

class StrategyController : HttpHandler {

    private fun handleGetStrategies(exchange: HttpExchange) {
        val strategies = listOf(
            StrategyInfo("clarkwright", "Algoritmo de Clark & Wright - Economias"),
            StrategyInfo("nearestneighbor", "Algoritmo do Vizinho Mais Próximo")
        )
        
        val response = JsonUtil.toJson(strategies)
        respond(exchange, 200, response)
    }

    override fun handle(exchange: HttpExchange) {
        try {
            when (exchange.requestMethod) {
                "GET" -> handleGetStrategies(exchange)
                else -> {
                    exchange.sendResponseHeaders(405, -1)
                }
            }
        } catch(e: Exception) {
            e.printStackTrace()
            respond(exchange, 500, """{"error": "${e.message}"}""")
        }
    }

    private fun respond(exchange: HttpExchange, statusCode: Int, response: String) {
        exchange.responseHeaders.add("Content-Type", "application/json")
        exchange.sendResponseHeaders(statusCode, response.toByteArray().size.toLong())
        exchange.responseBody.use { os -> os.write(response.toByteArray()) }
        exchange.close()
    }
} 