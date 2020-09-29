package no.nav.syfo.prediksjon

import no.nav.syfo.database.DatabaseInterface
import no.nav.syfo.database.toList
import no.nav.syfo.prediksjon.input.PPrediksjonInput
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
            created
        ) VALUES (DEFAULT, ?, ?, ?, ?, ?, ?) RETURNING id
    """

fun DatabaseInterface.createPrediksjonInput(
    pPrediksjonInput: PPrediksjonInput
): Pair<Int, UUID> {
    val uuid = UUID.randomUUID().toString()
    val now = Timestamp.from(Instant.now())

    connection.use { connection ->
        val idList = connection.prepareStatement(queryCreatePrediksjonInput).use {
            it.setString(1, uuid)
            it.setString(2, pPrediksjonInput.fnr)
            it.setString(3, pPrediksjonInput.aktorId)
            it.setObject(4, pPrediksjonInput.tilfelleStartDate)
            it.setObject(5, pPrediksjonInput.tilfelleEndDate)
            it.setTimestamp(6, now)
            it.executeQuery().toList { getInt("id") }
        }

        if (idList.size != 1) {
            throw SQLException("Creating prediksjonInput failed, no rows affected.")
        }
        connection.commit()

        return Pair(idList.first(), UUID.fromString(uuid))
    }
}
