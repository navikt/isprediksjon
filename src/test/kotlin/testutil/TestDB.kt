package testutil

import no.nav.syfo.database.Database
import no.nav.syfo.database.DatabaseInterface
import no.nav.syfo.database.DbConfig
import no.nav.syfo.database.toList
import org.testcontainers.containers.PostgreSQLContainer
import java.sql.Connection
import java.sql.ResultSet

class DevDatabase(dbConfig: DbConfig) : Database(dbConfig, null)

class TestDB : DatabaseInterface {

    private val container = PostgreSQLContainer<Nothing>("postgres:11.1").apply {
        withDatabaseName("db_test")
        withUsername("username")
        withPassword("password")
    }

    private var db: DatabaseInterface
    override val connection: Connection
        get() = db.connection.apply { autoCommit = false }

    init {
        container.start()
        db = DevDatabase(
            DbConfig(
                jdbcUrl = container.jdbcUrl,
                username = "username",
                password = "password",
                databaseName = "db_test"
            )
        )
    }

    fun stop() {
        container.stop()
    }
}

fun Connection.dropData(table: String) {
    val query = "DELETE FROM $table"
    use { connection ->
        connection.prepareStatement(query).executeUpdate()
        connection.commit()
    }
}

fun querySykmelding(table: String): String {
    return """
    SELECT *
    FROM $table
    WHERE sykmelding_id = ?
    """
}

fun Connection.getSM(
    table: String,
    sykmeldingId: String
): List<String> {

    return use { connection ->
        connection.prepareStatement(querySykmelding(table)).use {
            it.setString(1, sykmeldingId)
            it.executeQuery().toList {
                toSmManuellBehandlingId()
            }
        }
    }
}

fun ResultSet.toSmManuellBehandlingId(): String = getString("sykmelding_id")
