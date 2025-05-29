package config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import java.time.Instant

object DatabaseConfig {
    // Tabela de clientes
    object Customers : Table() {
        val id = integer("id").autoIncrement()
        val posX = double("pos_x")
        val posY = double("pos_y")
        val createdAt = long("created_at").default(System.currentTimeMillis())
        
        override val primaryKey = PrimaryKey(id)
    }
    
    // Tabela de distribuidores
    object Distributors : Table() {
        val id = integer("id").autoIncrement()
        val posX = double("pos_x")
        val posY = double("pos_y")
        val createdAt = long("created_at").default(System.currentTimeMillis())
        
        override val primaryKey = PrimaryKey(id)
    }
    
    // Tabela de soluções
    object Solutions : Table() {
        val id = integer("id").autoIncrement()
        val strategyName = varchar("strategy_name", 50)
        val distributorId = integer("distributor_id").references(Distributors.id)
        val totalDistance = double("total_distance").default(0.0)
        val createdAt = long("created_at").default(System.currentTimeMillis())
        
        override val primaryKey = PrimaryKey(id)
    }
    
    // Tabela de rotas
    object Routes : Table() {
        val id = integer("id").autoIncrement()
        val solutionId = integer("solution_id").references(Solutions.id)
        val sequence = integer("sequence").default(0)
        val distance = double("distance").default(0.0)
        val createdAt = long("created_at").default(System.currentTimeMillis())
        
        override val primaryKey = PrimaryKey(id)
    }
    
    // Tabela de pontos da rota
    object RoutePoints : Table() {
        val id = integer("id").autoIncrement()
        val routeId = integer("route_id").references(Routes.id)
        val customerId = integer("customer_id").references(Customers.id).nullable()
        val distributorId = integer("distributor_id").references(Distributors.id).nullable()
        val sequenceNumber = integer("sequence_number")
        
        override val primaryKey = PrimaryKey(id)
    }
    
    fun init() {
        val dbHost = System.getenv("DB_HOST") ?: "localhost"
        val dbPort = System.getenv("DB_PORT") ?: "5432"
        val dbName = System.getenv("DB_NAME") ?: "clark_wright"
        val dbUser = System.getenv("DB_USER") ?: "postgres"
        val dbPassword = System.getenv("DB_PASSWORD") ?: "postgres"

        println("Conectando ao banco de dados: jdbc:postgresql://$dbHost:$dbPort/$dbName")
        
        val config = HikariConfig().apply {
            jdbcUrl = "jdbc:postgresql://$dbHost:$dbPort/$dbName"
            driverClassName = "org.postgresql.Driver"
            username = dbUser
            password = dbPassword
            maximumPoolSize = 10
        }
        
        val dataSource = HikariDataSource(config)
        Database.connect(dataSource)
        
        // Apagar e recriar todas as tabelas para garantir que o esquema seja atualizado
        transaction {
            // Primeiro removemos as tabelas na ordem inversa de dependência
            SchemaUtils.drop(RoutePoints)
            SchemaUtils.drop(Routes)
            SchemaUtils.drop(Solutions)
            SchemaUtils.drop(Customers)
            SchemaUtils.drop(Distributors)
            
            // Agora criamos as tabelas novamente
            SchemaUtils.create(Distributors)
            SchemaUtils.create(Customers)
            SchemaUtils.create(Solutions)
            SchemaUtils.create(Routes)
            SchemaUtils.create(RoutePoints)
            
            println("Banco de dados reinicializado com sucesso!")
        }
    }
} 