package repositories

import config.DatabaseConfig
import domain.entities.Customer
import domain.entities.Position
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class CustomerRepository {
    companion object {
        private var instance: CustomerRepository? = null
        fun getInstance(): CustomerRepository {
            if (instance == null) {
                instance = CustomerRepository()
            }
            return instance!!
        }
    }

    fun save(position: Position): Customer {
        var customerId = 0
        transaction {
            customerId = DatabaseConfig.Customers.insert {
                it[posX] = position.x
                it[posY] = position.y
            } get DatabaseConfig.Customers.id
        }
        return Customer(customerId, position)
    }

    fun findAll(): List<Customer> {
        return transaction {
            DatabaseConfig.Customers.selectAll().map { row ->
                Customer(
                    id = row[DatabaseConfig.Customers.id],
                    position = Position(
                        x = row[DatabaseConfig.Customers.posX],
                        y = row[DatabaseConfig.Customers.posY]
                    )
                )
            }
        }
    }

    fun findById(id: Int): Customer? {
        return transaction {
            DatabaseConfig.Customers.select { DatabaseConfig.Customers.id eq id }
                .map { row ->
                    Customer(
                        id = row[DatabaseConfig.Customers.id],
                        position = Position(
                            x = row[DatabaseConfig.Customers.posX],
                            y = row[DatabaseConfig.Customers.posY]
                        )
                    )
                }
                .singleOrNull()
        }
    }

    fun update(customer: Customer): Boolean {
        return transaction {
            val updatedRows = DatabaseConfig.Customers.update({ DatabaseConfig.Customers.id eq customer.id }) {
                it[posX] = customer.position.x
                it[posY] = customer.position.y
            }
            updatedRows > 0
        }
    }

    fun deleteById(id: Int): Boolean {
        return transaction {
            val deletedRows = DatabaseConfig.Customers.deleteWhere { DatabaseConfig.Customers.id eq id }
            deletedRows > 0
        }
    }
}
