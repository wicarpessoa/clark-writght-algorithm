package strategies

import SortBySaving
import domain.entities.Customer
import domain.entities.Distributor
import domain.entities.Node
import domain.entities.Route
import kotlin.math.min

class ClarkWrightAlgorithm: RoutingStrategy {
    private fun findRouteByCustomer(routes: List<Route>, customer: Customer): Route? {
        return routes.find { it.containsCustomer(customer) }
    }
    
    /**
     * Implementação melhorada do algoritmo Clark-Wright para roteamento de veículos.
     * 
     * O algoritmo funciona da seguinte forma:
     * 1. Calcula a economia de distância (savings) para cada par de clientes
     * 2. Ordena os pares de clientes por economia em ordem decrescente
     * 3. Para cada par, tenta unir rotas ou criar novas rotas com base na economia
     * 4. Trata casos especiais como clientes isolados
     */
    override fun solve(distributor: Distributor, customers: List<Customer>): List<Route> {
        if (customers.isEmpty()) {
            return emptyList()
        }
        
        // Criar rotas iniciais (1 rota para cada cliente, indo do depósito para o cliente e voltando)
        val initialRoutes = mutableListOf<Route>()
        for (customer in customers) {
            val route = Route()
            route.add(customer)
            route.calculatePath(distributor)
            initialRoutes.add(route)
        }
        
        // Se houver apenas um cliente, já temos a solução
        if (customers.size == 1) {
            return initialRoutes
        }
        
        // Calcular todas as economias entre pares de clientes
        val nodes = mutableListOf<Node>()
        for (i in 0 until customers.size) {
            val customerI = customers[i]
            val distanceFromOriginToI = distributor.position.calculateDistance(customerI.position)
            
            for (j in 0 until i) {
                val customerJ = customers[j]
                val distanceFromOriginToJ = distributor.position.calculateDistance(customerJ.position)
                val distanceBetweenCustomers = customerI.position.calculateDistance(customerJ.position)
                
                // A economia é a redução na distância quando se visita i e j na mesma rota
                // em vez de visitá-los separadamente a partir da origem (distribuidor)
                val saving = distanceFromOriginToI + distanceFromOriginToJ - distanceBetweenCustomers
                
                val newNode = Node(customerJ, customerI, saving)
                nodes.add(newNode)
            }
        }
        
        // Ordenar os nós por economia em ordem decrescente
        nodes.sortWith(SortBySaving())
        
        // Fase de construção de rotas
        val routes = mutableListOf<Route>()
        
        // Para cada economia (par de clientes), tentar unir rotas ou criar novas rotas
        for (node in nodes) {
            val customerFrom = node.from
            val customerTo = node.to
            
            // Verificar se algum dos clientes já está em uma rota
            val routeFrom = findRouteByCustomer(routes, customerFrom)
            val routeTo = findRouteByCustomer(routes, customerTo)
            
            if (routeFrom == null && routeTo == null) {
                // Nenhum dos clientes está em uma rota, criar uma nova rota
                val newRoute = Route()
                newRoute.add(customerFrom)
                newRoute.add(customerTo)
                newRoute.calculatePath(distributor)
                routes.add(newRoute)
            } else if (routeFrom != null && routeTo == null) {
                // Apenas o cliente "from" está em uma rota, tentar adicionar "to" a essa rota
                if (isEndpoint(routeFrom, customerFrom)) {
                    // Garantir que "from" esteja no final da rota para facilitar a adição
                    if (routeFrom.customers.first == customerFrom) {
                        routeFrom.reverse()
                    }
                    // Adicionar "to" ao final da rota
                    routeFrom.add(customerTo)
                    routeFrom.calculatePath(distributor)
                }
            } else if (routeFrom == null && routeTo != null) {
                // Apenas o cliente "to" está em uma rota, tentar adicionar "from" a essa rota
                if (isEndpoint(routeTo, customerTo)) {
                    // Garantir que "to" esteja no final da rota para facilitar a adição
                    if (routeTo.customers.first == customerTo) {
                        routeTo.reverse()
                    }
                    // Adicionar "from" ao final da rota
                    routeTo.add(customerFrom)
                    routeTo.calculatePath(distributor)
                }
            } else if (routeFrom !== routeTo) {
                // Os dois clientes estão em rotas diferentes, tentar unir as rotas
                mergeRoutes(routeFrom!!, routeTo!!, customerFrom, customerTo, distributor, routes)
            }
        }
        
        // Verificar se há clientes isolados que não foram adicionados a nenhuma rota
        for (initialRoute in initialRoutes) {
            val customer = initialRoute.customers.first()
            if (!routes.any { it.containsCustomer(customer) }) {
                // Este cliente não foi incluído em nenhuma rota, adicionar sua rota inicial
                routes.add(initialRoute)
            }
        }
        
        // Recalcular o caminho completo para todas as rotas
        routes.forEach { it.calculatePath(distributor) }
        
        return routes
    }
    
    /**
     * Verifica se o cliente é um ponto final (primeiro ou último) da rota
     */
    private fun isEndpoint(route: Route, customer: Customer): Boolean {
        return route.customers.first == customer || route.customers.last == customer
    }
    
    /**
     * Tenta unir duas rotas, garantindo que a união aconteça apenas nos extremos
     */
    private fun mergeRoutes(routeFrom: Route, routeTo: Route, customerFrom: Customer, customerTo: Customer, 
                            distributor: Distributor, routes: MutableList<Route>) {
        // Verifica se é possível unir as rotas (os clientes devem estar nas extremidades)
        val fromIsFirst = routeFrom.customers.first == customerFrom
        val fromIsLast = routeFrom.customers.last == customerFrom
        val toIsFirst = routeTo.customers.first == customerTo
        val toIsLast = routeTo.customers.last == customerTo
        
        if ((fromIsFirst || fromIsLast) && (toIsFirst || toIsLast)) {
            // É possível unir as rotas. Ajusta as rotas para que a união ocorra nas extremidades corretas
            if (fromIsFirst) routeFrom.reverse()
            if (toIsLast) routeTo.reverse()
            
            // Agora garantimos que 'from' está no final de routeFrom e 'to' está no início de routeTo
            // Adiciona todos os clientes de routeTo a routeFrom
            routeFrom.customers.addAll(routeTo.customers)
            
            // Recalcula o caminho
            routeFrom.calculatePath(distributor)
            
            // Remove routeTo da lista de rotas
            routes.remove(routeTo)
        }
    }
}
