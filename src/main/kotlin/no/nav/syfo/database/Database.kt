package no.nav.syfo.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

import org.flywaydb.core.Flyway
import java.sql.Connection

enum class Role {
    ADMIN, USER, READONLY;

    override fun toString() = name.toLowerCase()
}

data class DbConfig(
    val jdbcUrl: String,
    val password: String,
    val username: String,
    val databaseName: String,
    val poolSize: Int = 2,
    val runMigrationsOninit: Boolean = true
)

class ProdDatabase(dbConfig: DbConfig, initBlock: (context: Database) -> Unit) : Database(dbConfig, initBlock) {

    override fun runFlywayMigrations(jdbcUrl: String, username: String, password: String): Int =
        Flyway.configure().run {
            dataSource(jdbcUrl, username, password)
            initSql("SET ROLE \"${dbConfig.databaseName}-${Role.ADMIN}\"") // required for assigning proper owners for the tables
            load().migrate()
        }
}

abstract class Database(val dbConfig: DbConfig, private val initBlock: ((context: Database) -> Unit)?) :
    DatabaseInterface {

    private var dataSource: HikariDataSource = HikariDataSource(
        HikariConfig().apply {
            jdbcUrl = dbConfig.jdbcUrl
            username = dbConfig.username
            password = dbConfig.password
            maximumPoolSize = dbConfig.poolSize
            minimumIdle = 1
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }
    )

    init {

        afterInit()
    }

    fun updateCredentials(username: String, password: String) {
        dataSource.apply {
            hikariConfigMXBean.setPassword(password)
            hikariConfigMXBean.setUsername(username)
            hikariPoolMXBean.softEvictConnections()
        }
    }

    override val connection: Connection
        get() = dataSource.connection

    private fun afterInit() {
        if (dbConfig.runMigrationsOninit) {
            runFlywayMigrations(dbConfig.jdbcUrl, dbConfig.username, dbConfig.password)
        }
        initBlock?.let { run(it) }
    }

    open fun runFlywayMigrations(jdbcUrl: String, username: String, password: String) = Flyway.configure().run {
        dataSource(jdbcUrl, username, password)
        load().migrate()
    }
}

interface DatabaseInterface {
    val connection: Connection
}
