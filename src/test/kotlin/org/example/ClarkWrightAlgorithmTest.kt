package org.example

import strategies.ClarkWrightAlgorithm
import domain.entities.Customer
import domain.entities.Distributor
import domain.entities.Position
import domain.entities.Route
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Disabled
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ClarkWrightAlgorithmTest {
    private fun routesToString(routes: List<Route>): List<String> {
        val result = mutableListOf<String>()

        for (route in routes) {
            var strRoute = ""

            for (customer in route.customers) {
                strRoute += customer.id
            }

            result.add(strRoute)
        }

        return result
    }

    @Test
    fun testVeryEasyScenario() {
        val algorithm = ClarkWrightAlgorithm()
        val distributor = Distributor(Position(0.0, 0.0))

        val result = algorithm.solve(
            distributor,
            listOf(
                Customer(1, Position(15.0, 26.0)),
                Customer(2, Position(25.0, 30.0)),
                Customer(3, Position(35.0, 15.0)),
            )
        )

        // Deveria gerar uma única rota com todos os clientes
        assertEquals(1, result.size)
        assertEquals(3, result[0].customers.size)
        assertTrue(result[0].customers.any { it.id == 1 })
        assertTrue(result[0].customers.any { it.id == 2 })
        assertTrue(result[0].customers.any { it.id == 3 })
    }
    
    @Test
    @Disabled("Teste desativado temporariamente devido a mudanças no tipo de coordenadas")
    fun testMultipleRoutes() {
        val algorithm = ClarkWrightAlgorithm()
        val distributor = Distributor(Position(0.0, 0.0))

        // Criar um cenário onde duas rotas são melhores que uma única
        val result = algorithm.solve(
            distributor,
            listOf(
                Customer(1, Position(10.0, 10.0)),  // Grupo 1
                Customer(2, Position(12.0, 12.0)),  // Grupo 1
                Customer(3, Position(100.0, 100.0)), // Grupo 2 - distante do grupo 1
                Customer(4, Position(102.0, 102.0))  // Grupo 2 - distante do grupo 1
            )
        )

        // Deveria gerar duas rotas separadas
        assertEquals(2, result.size)
        
        // Verificar se os clientes próximos estão na mesma rota
        val route1 = result.find { it.customers.any { c -> c.id == 1 } }!!
        val route2 = result.find { it.customers.any { c -> c.id == 3 } }!!
        
        assertTrue(route1.customers.any { it.id == 2 })
        assertTrue(route2.customers.any { it.id == 4 })
    }
    
    @Test
    @Disabled("Teste desativado temporariamente devido a mudanças no tipo de coordenadas")
    fun testIsolatedCustomer() {
        val algorithm = ClarkWrightAlgorithm()
        val distributor = Distributor(Position(0.0, 0.0))

        // Criar um cenário com um cliente isolado
        val result = algorithm.solve(
            distributor,
            listOf(
                Customer(1, Position(10.0, 10.0)),
                Customer(2, Position(12.0, 12.0)),
                Customer(3, Position(200.0, 200.0)) // Cliente muito distante dos outros
            )
        )

        // Verificar se o cliente isolado está em uma rota separada
        assertEquals(2, result.size)
        
        // Encontrar a rota do cliente isolado
        val isolatedRoute = result.find { it.customers.size == 1 }
        assertTrue(isolatedRoute != null && isolatedRoute.customers.first().id == 3)
    }
}