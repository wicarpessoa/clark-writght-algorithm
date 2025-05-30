package config

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler

class CorsHandler(private val handler: HttpHandler) : HttpHandler {
    override fun handle(exchange: HttpExchange) {
        val origin = exchange.requestHeaders.getFirst("Origin")
        val method = exchange.requestMethod
        println("DEBUG - Request details:")
        println("Origin: $origin")
        println("Method: $method")
        println("URI: ${exchange.requestURI}")
        println("Headers: ${exchange.requestHeaders}")
        
        // Set CORS headers
        exchange.responseHeaders.add("Access-Control-Allow-Origin", "http://34.205.72.237")
        exchange.responseHeaders.add("Access-Control-Allow-Methods", "GET, POST, OPTIONS, PUT, DELETE")
        exchange.responseHeaders.add("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With")
        exchange.responseHeaders.add("Access-Control-Allow-Credentials", "true")
        exchange.responseHeaders.add("Access-Control-Max-Age", "86400")
        exchange.responseHeaders.add("Access-Control-Expose-Headers", "*")

        // Handle preflight requests
        if (method == "OPTIONS") {
            println("DEBUG - Handling OPTIONS request")
            exchange.sendResponseHeaders(204, -1)
            return
        }

        // Handle actual request
        println("DEBUG - Handling $method request to ${exchange.requestURI}")
        handler.handle(exchange)
    }
} 