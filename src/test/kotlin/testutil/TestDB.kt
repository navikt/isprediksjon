package testutil

import no.nav.syfo.database.Database
import no.nav.syfo.database.DatabaseInterface
import no.nav.syfo.database.DbConfig
import no.nav.syfo.database.toList
import no.nav.syfo.domain.Fodselsnummer
import no.nav.syfo.prediksjon.input.PPrediksjonInput
import org.testcontainers.containers.PostgreSQLContainer
import java.sql.Connection
import java.sql.ResultSet
import java.time.LocalDate

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

const val queryGetPrediksjonInput =
    """
    SELECT *
    FROM prediksjon_input
    WHERE fnr = ?
    """

fun Connection.getPrediksjonInput(
    fnr: Fodselsnummer
): List<PPrediksjonInput> {
    return use { connection ->
        connection.prepareStatement(queryGetPrediksjonInput).use {
            it.setString(1, fnr.value)
            it.executeQuery().toList {
                toPPrediksjonInput()
            }
        }
    }
}

const val queryGetPrediksjonInput1 =
    """
    SELECT *
    FROM prediksjon_input
    """

fun Connection.getPrediksjonInput1(): List<PPrediksjonInput> {
    return use { connection ->
        connection.prepareStatement(queryGetPrediksjonInput1).use {
            it.executeQuery().toList {
                toPPrediksjonInput()
            }
        }
    }
}

fun ResultSet.toPPrediksjonInput(): PPrediksjonInput =
    PPrediksjonInput(
        id = getInt("id"),
        uuid = getString("uuid"),
        fnr = getString("fnr"),
        aktorId = getString("aktorid"),
        tilfelleStartDate = getObject("tilfelle_start_date", LocalDate::class.java),
        tilfelleEndDate = getObject("tilfelle_end_date", LocalDate::class.java),
        created = getTimestamp("created").toLocalDateTime()
    )
