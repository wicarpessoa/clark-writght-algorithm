package config

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler

class CorsHandler(private val handler: HttpHandler) : HttpHandler {
    override fun handle(exchange: HttpExchange) {
        // Set CORS headers
        exchange.responseHeaders.add("Access-Control-Allow-Origin", "https://clark-wright-algorithm-front.vercel.app")
        exchange.responseHeaders.add("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
        exchange.responseHeaders.add("Access-Control-Allow-Headers", "Content-Type")
        exchange.responseHeaders.add("Access-Control-Max-Age", "86400")

        // Handle preflight requests
        if (exchange.requestMethod == "OPTIONS") {
            exchange.sendResponseHeaders(204, -1)
            return
        }

        // Handle actual request
        handler.handle(exchange)
    }
} 