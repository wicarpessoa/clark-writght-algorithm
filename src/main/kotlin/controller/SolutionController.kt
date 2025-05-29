package controller

import application.Registry
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import repositories.SolutionRepository
import utils.JsonUtil

class SolutionController : HttpHandler {
    private val solutionRepository = SolutionRepository.getInstance()

    private fun handleGetAllSolutions(exchange: HttpExchange) {
        val solutions = solutionRepository.getAllSolutions()
        val response = JsonUtil.toJson(solutions)
        respond(exchange, 200, response)
    }

    override fun handle(exchange: HttpExchange) {
        try {
            when (exchange.requestMethod) {
                "GET" -> handleGetAllSolutions(exchange)
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
        exchange.responseHeaders.add("Access-Control-Allow-Origin", "https://clark-wright-algorithm-front-ljfkge6ds-wicar-pessoas-projects.vercel.app")
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