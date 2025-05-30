package config

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler

class CorsHandler(private val handler: HttpHandler) : HttpHandler {
    override fun handle(exchange: HttpExchange) {
        // Set CORS headers
        exchange.responseHeaders.add("Access-Control-Allow-Origin", "http://34.205.72.237")
        exchange.responseHeaders.add("Access-Control-Allow-Methods", "GET, OPTIONS")
        exchange.responseHeaders.add("Access-Control-Allow-Headers", "Content-Type")
        exchange.responseHeaders.add("Access-Control-Allow-Credentials", "true")

        // Handle preflight requests
        if (exchange.requestMethod == "OPTIONS") {
            exchange.sendResponseHeaders(204, -1)
            return
        }

        // Handle actual request
        handler.handle(exchange)
    }
} 