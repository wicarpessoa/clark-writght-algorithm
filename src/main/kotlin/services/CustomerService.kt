package services

import domain.entities.Customer
import domain.entities.Position
import repositories.CustomerRepository

class CustomerService(
    private val repository: CustomerRepository = CustomerRepository.getInstance()
) {
    //esse position supostamente era pra ser um DTO, mas ta bom ja rs
    fun create(position: Position): Customer {
        return repository.save(position)
    }

    fun getAll(): List<Customer> {
        return repository.findAll()
    }

    fun getById(id: Int): Customer? {
        return repository.findById(id)
    }

    fun update(id: Int, newX: Double, newY: Double): Boolean {
        val customer = repository.findById(id) ?: return false
        val updated = Customer(id, Position(newX, newY))
        return repository.update(updated)
    }

    fun delete(id: Int): Boolean {
        return repository.deleteById(id)
    }
}