package repositories

import config.DatabaseConfig
import domain.entities.Distributor
import domain.entities.Position
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class DistributorRepository private constructor() {
    companion object {
        private var instance: DistributorRepository? = null
        fun getInstance(): DistributorRepository {
            if (instance == null) {
                instance = DistributorRepository()
            }
            return instance!!
        }
    }

    fun save(distributor: Distributor): Distributor {
        // Check if there's already a distributor (we only allow one)
        val existingId = transaction {
            DatabaseConfig.Distributors.selectAll().map { it[DatabaseConfig.Distributors.id] }.singleOrNull()
        }
        
        val distributorId = transaction {
            if (existingId != null) {
                // Update existing distributor
                DatabaseConfig.Distributors.update({ DatabaseConfig.Distributors.id eq existingId }) {
                    it[posX] = distributor.position.x
                    it[posY] = distributor.position.y
                }
                existingId
            } else {
                // Insert new distributor
                DatabaseConfig.Distributors.insert {
                    it[posX] = distributor.position.x
                    it[posY] = distributor.position.y
                } get DatabaseConfig.Distributors.id
            }
        }
        
        // Retorna o distribuidor com o ID atribuído
        return distributor.copy(id = distributorId)
    }

    fun find(): Distributor {
        return transaction {
            val row = DatabaseConfig.Distributors.selectAll().firstOrNull() 
                ?: throw IllegalStateException("Distribuidor não foi atribuído ainda")
            
            Distributor(
                position = Position(
                    x = row[DatabaseConfig.Distributors.posX],
                    y = row[DatabaseConfig.Distributors.posY]
                ),
                id = row[DatabaseConfig.Distributors.id]
            )
        }
    }
}