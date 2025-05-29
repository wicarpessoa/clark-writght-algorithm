package domain.entities

import kotlinx.serialization.Serializable
import serializers.LinkedListCustomerSerializer
import java.util.LinkedList
import domain.entities.Distributor
import domain.entities.Position

@Serializable
class Route {
    @Serializable(with = LinkedListCustomerSerializer::class)
    val customers = LinkedList<Customer>()
    
    // Lista de posições completas da rota para desenho no frontend
    @Serializable
    var path: List<Position>? = null
    
    // Distância total da rota
    var totalDistance: Double = 0.0

    fun add(customer: Customer) {
        customers.add(customer)
    }
    
    fun addFirst(customer: Customer) {
        customers.addFirst(customer)
    }

    fun getCustomers(): List<Customer> {
        return customers
    }

    fun containsCustomer(customer: Customer): Boolean {
        return customers.any { it.id == customer.id }
    }
    
    // Calcula a distância total da rota incluindo o retorno ao depósito
    fun calculateTotalDistance(distributor: Distributor) {
        if (customers.isEmpty()) {
            totalDistance = 0.0
            return
        }
        
        var distance = 0.0
        
        // Distância do depósito ao primeiro cliente
        distance += distributor.position.calculateDistance(customers.first.position)
        
        // Distância entre clientes consecutivos
        var prev = customers.first
        for (i in 1 until customers.size) {
            val current = customers[i]
            distance += prev.position.calculateDistance(current.position)
            prev = current
        }
        
        // Distância do último cliente de volta ao depósito
        distance += customers.last.position.calculateDistance(distributor.position)
        
        totalDistance = distance
    }
    
    // Calcula o caminho completo incluindo o distribuidor como origem/destino
    fun calculatePath(distributor: Distributor) {
        if (customers.isEmpty()) {
            path = emptyList()
            return
        }
        
        val pathPoints = mutableListOf<Position>()
        
        // Adiciona o distribuidor como ponto inicial
        pathPoints.add(distributor.position)
        
        // Adiciona todos os clientes na ordem da rota
        customers.forEach { pathPoints.add(it.position) }
        
        // Adiciona o distribuidor como ponto final (volta para a origem)
        pathPoints.add(distributor.position)
        
        path = pathPoints
        
        // Atualiza a distância total
        calculateTotalDistance(distributor)
    }
    
    // Inverte a ordem da rota para melhorar fusões
    fun reverse() {
        val temp = LinkedList<Customer>()
        while (customers.isNotEmpty()) {
            temp.addFirst(customers.removeFirst())
        }
        customers.addAll(temp)
    }
}
