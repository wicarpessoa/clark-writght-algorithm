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
        exchange.sendResponseHeaders(statusCode, response.toByteArray().size.toLong())
        exchange.responseBody.use { os -> os.write(response.toByteArray()) }
        exchange.close()
    }
} 