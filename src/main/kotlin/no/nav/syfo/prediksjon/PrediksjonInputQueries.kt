package no.nav.syfo.prediksjon

import no.nav.syfo.database.DatabaseInterface
import no.nav.syfo.database.toList
import no.nav.syfo.oppfolgingstilfelle.domain.PersonOppfolgingstilfelle
import java.sql.SQLException
import java.sql.Timestamp
import java.time.Instant
import java.util.*

const val queryCreatePrediksjonInput =
    """
        INSERT INTO prediksjon_input(
            id,
            uuid,
            fnr,
            aktorid,
            tilfelle_start_date,
            tilfelle_end_date,
            input_created
        ) VALUES (DEFAULT, ?, ?, ?, ?, ?, ?) RETURNING id
    """

fun DatabaseInterface.createPrediksjonInput(
    personOppfolgingstilfelle: PersonOppfolgingstilfelle
): Pair<Int, UUID> {
    val uuid = UUID.randomUUID()
    val now = Timestamp.from(Instant.now())

    connection.use { connection ->
        val idList = connection.prepareStatement(queryCreatePrediksjonInput).use {
            it.setString(1, uuid.toString())
            it.setString(2, personOppfolgingstilfelle.fnr.value)
            it.setString(3, personOppfolgingstilfelle.aktorId.value)
            it.setObject(4, personOppfolgingstilfelle.tilfelleStartDate)
            it.setObject(5, personOppfolgingstilfelle.tilfelleEndDate)
            it.setTimestamp(6, now)
            it.executeQuery().toList { getInt("id") }
        }

        if (idList.size != 1) {
            throw SQLException("Creating prediksjonInput failed, no rows affected.")
        }
        connection.commit()

        return Pair(idList.first(), uuid)
    }
}
