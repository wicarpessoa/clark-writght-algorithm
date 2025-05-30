package config

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler

class CorsHandler(private val handler: HttpHandler) : HttpHandler {
    override fun handle(exchange: HttpExchange) {
        val origin = exchange.requestHeaders.getFirst("Origin")
        println("Received request from origin: $origin")
        
        // Set CORS headers
        exchange.responseHeaders.add("Access-Control-Allow-Origin", "*")
        exchange.responseHeaders.add("Access-Control-Allow-Methods", "*")
        exchange.responseHeaders.add("Access-Control-Allow-Headers", "*")
        exchange.responseHeaders.add("Access-Control-Max-Age", "86400")

        // Handle preflight requests
        if (exchange.requestMethod == "OPTIONS") {
            println("Handling OPTIONS request")
            exchange.sendResponseHeaders(204, -1)
            return
        }

        // Handle actual request
        println("Handling ${exchange.requestMethod} request to ${exchange.requestURI}")
        handler.handle(exchange)
    }
} 