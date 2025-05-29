package repositories

import config.DatabaseConfig
import domain.entities.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import utils.JsonUtil
import java.time.Instant
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.and

class SolutionRepository {
    companion object {
        private var instance: SolutionRepository? = null
        fun getInstance(): SolutionRepository {
            if (instance == null) {
                instance = SolutionRepository()
            }
            return instance!!
        }
    }

    fun saveSolution(strategyName: String, distributor: Distributor, routes: List<Route>): Int {
        var solutionId = 0
        
        transaction {
            // 1. Salva a solução
            solutionId = DatabaseConfig.Solutions.insert {
                it[DatabaseConfig.Solutions.strategyName] = strategyName
                it[DatabaseConfig.Solutions.distributorId] = distributor.id ?: 
                    throw IllegalStateException("O distribuidor deve estar salvo antes de criar uma solução")
                it[DatabaseConfig.Solutions.totalDistance] = routes.sumOf { route -> route.totalDistance }
            } get DatabaseConfig.Solutions.id
            
            // 2. Salva cada rota
            routes.forEachIndexed { index, route ->
                val routeId = DatabaseConfig.Routes.insert {
                    it[DatabaseConfig.Routes.solutionId] = solutionId
                    it[DatabaseConfig.Routes.sequence] = index
                    it[DatabaseConfig.Routes.distance] = route.totalDistance
                } get DatabaseConfig.Routes.id
                
                // 3. Salva os pontos da rota
                val routePath = route.path ?: emptyList()
                routePath.forEachIndexed { seqNum, position ->
                    // Determina se o ponto é o distribuidor ou um cliente
                    val isDistributorPoint = seqNum == 0 || seqNum == routePath.size - 1
                    
                    if (isDistributorPoint) {
                        // Ponto é o distribuidor (início ou fim da rota)
                        DatabaseConfig.RoutePoints.insert {
                            it[DatabaseConfig.RoutePoints.routeId] = routeId
                            it[DatabaseConfig.RoutePoints.distributorId] = distributor.id
                            it[DatabaseConfig.RoutePoints.customerId] = null
                            it[DatabaseConfig.RoutePoints.sequenceNumber] = seqNum
                        }
                    } else {
                        // Ponto é um cliente
                        val customer = route.customers.getOrNull(seqNum - 1)
                        if (customer != null) {
                            DatabaseConfig.RoutePoints.insert {
                                it[DatabaseConfig.RoutePoints.routeId] = routeId
                                it[DatabaseConfig.RoutePoints.distributorId] = null
                                it[DatabaseConfig.RoutePoints.customerId] = customer.id
                                it[DatabaseConfig.RoutePoints.sequenceNumber] = seqNum
                            }
                        }
                    }
                }
            }
        }
        
        return solutionId
    }

    fun getAllSolutions(): List<Solution> {
        return transaction {
            // Busca todas as soluções
            DatabaseConfig.Solutions.selectAll().map { solutionRow ->
                val solutionId = solutionRow[DatabaseConfig.Solutions.id]
                
                // Busca as rotas de cada solução
                val routes = DatabaseConfig.Routes.select { 
                    DatabaseConfig.Routes.solutionId eq solutionId 
                }.map { routeRow ->
                    val routeId = routeRow[DatabaseConfig.Routes.id]
                    
                    // Busca os pontos de cada rota
                    val points = DatabaseConfig.RoutePoints.select {
                        DatabaseConfig.RoutePoints.routeId eq routeId
                    }.orderBy(DatabaseConfig.RoutePoints.sequenceNumber).map { pointRow ->
                        RoutePoint(
                            id = pointRow[DatabaseConfig.RoutePoints.id],
                            routeId = routeId,
                            customerId = pointRow[DatabaseConfig.RoutePoints.customerId],
                            distributorId = pointRow[DatabaseConfig.RoutePoints.distributorId],
                            sequenceNumber = pointRow[DatabaseConfig.RoutePoints.sequenceNumber]
                        )
                    }
                    
                    RouteWithPoints(
                        id = routeId,
                        solutionId = solutionId,
                        sequence = routeRow[DatabaseConfig.Routes.sequence],
                        distance = routeRow[DatabaseConfig.Routes.distance],
                        points = points,
                        createdAt = Instant.ofEpochMilli(routeRow[DatabaseConfig.Routes.createdAt])
                    )
                }
                
                Solution(
                    id = solutionId,
                    strategyName = solutionRow[DatabaseConfig.Solutions.strategyName],
                    distributorId = solutionRow[DatabaseConfig.Solutions.distributorId],
                    totalDistance = solutionRow[DatabaseConfig.Solutions.totalDistance],
                    routes = routes,
                    createdAt = Instant.ofEpochMilli(solutionRow[DatabaseConfig.Solutions.createdAt])
                )
            }
        }
    }
    
    fun getSolutionById(id: Int): Solution? {
        return transaction {
            DatabaseConfig.Solutions.select {
                DatabaseConfig.Solutions.id eq id
            }.map { solutionRow ->
                val solutionId = solutionRow[DatabaseConfig.Solutions.id]
                
                // Busca as rotas da solução
                val routes = DatabaseConfig.Routes.select { 
                    DatabaseConfig.Routes.solutionId eq solutionId 
                }.map { routeRow ->
                    val routeId = routeRow[DatabaseConfig.Routes.id]
                    
                    // Busca os pontos da rota
                    val points = DatabaseConfig.RoutePoints.select {
                        DatabaseConfig.RoutePoints.routeId eq routeId
                    }.orderBy(DatabaseConfig.RoutePoints.sequenceNumber).map { pointRow ->
                        RoutePoint(
                            id = pointRow[DatabaseConfig.RoutePoints.id],
                            routeId = routeId,
                            customerId = pointRow[DatabaseConfig.RoutePoints.customerId],
                            distributorId = pointRow[DatabaseConfig.RoutePoints.distributorId],
                            sequenceNumber = pointRow[DatabaseConfig.RoutePoints.sequenceNumber]
                        )
                    }
                    
                    RouteWithPoints(
                        id = routeId,
                        solutionId = solutionId,
                        sequence = routeRow[DatabaseConfig.Routes.sequence],
                        distance = routeRow[DatabaseConfig.Routes.distance],
                        points = points,
                        createdAt = Instant.ofEpochMilli(routeRow[DatabaseConfig.Routes.createdAt])
                    )
                }
                
                Solution(
                    id = solutionId,
                    strategyName = solutionRow[DatabaseConfig.Solutions.strategyName],
                    distributorId = solutionRow[DatabaseConfig.Solutions.distributorId],
                    totalDistance = solutionRow[DatabaseConfig.Solutions.totalDistance],
                    routes = routes,
                    createdAt = Instant.ofEpochMilli(solutionRow[DatabaseConfig.Solutions.createdAt])
                )
            }.singleOrNull()
        }
    }
} 