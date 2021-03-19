package no.nav.syfo.prediksjon

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.server.testing.*
import io.mockk.unmockkAll
import no.nav.syfo.database.DatabaseInterface
import no.nav.syfo.database.toList
import no.nav.syfo.domain.AktorId
import no.nav.syfo.domain.Fodselsnummer
import org.amshove.kluent.shouldBeEqualTo
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import testutil.TestDB
import java.sql.SQLException
import java.sql.Timestamp
import java.time.Instant
import java.time.OffsetDateTime
import java.util.*

object PrediksjonOutputSpek : Spek({

    val database = TestDB()

    afterGroup {
        database.stop()
        unmockkAll()
    }

    describe("PrediksjonOutput") {
        with(TestApplicationEngine()) {
            start()
        }

        describe("Should store and get from prediksjon_output") {
            val fnr = Fodselsnummer("11111111111")
            val aktorid = AktorId("2222222222222")

            database.createPrediksjonOutputTest(
                PrediksjonOutput(
                    fnr,
                    aktorid,
                    OffsetDateTime.now(),
                    OffsetDateTime.now(),
                    "OK",
                    0.95f,
                    ForklaringFrontend(listOf("diagnosis", "md"), listOf("grad", "time", "hist"))
                ),
                1
            )
            it("Should return 1 prediksjon ") {
                val prediksjonList = database.getPrediksjon(fnr)

                prediksjonList.size shouldBeEqualTo 1
            }
        }
    }
})

const val queryCreatePrediksjonOutput =
    """
        INSERT INTO prediksjon_output(
            id,
            input_id,
            fnr,
            aktorid,
            tilfelle_start_date,
            tilfelle_end_date,
            input_created,
            prediksjon_created,
            datastate,
            prediksjon_delta,
            forklaring_raw,
            forklaring_front_end
        ) VALUES (DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::JSON, ?::JSON) RETURNING id
    """

fun DatabaseInterface.createPrediksjonOutputTest(
    prediksjon: PrediksjonOutput,
    inputId: Int
): Pair<Int, UUID> {
    val uuid = UUID.randomUUID().toString()
    val now = Timestamp.from(Instant.now())

    val forklaringRaw = "{\"key_0\": 1.1, \"key_1\": 1.3}"

    val mapper = jacksonObjectMapper()

    connection.use { connection ->
        val idList = connection.prepareStatement(queryCreatePrediksjonOutput).use {
            it.setInt(1, inputId)
            it.setString(2, prediksjon.fnr.value)
            it.setString(3, prediksjon.aktorId.value)
            it.setObject(4, prediksjon.tilfelleStartDate)
            it.setObject(5, prediksjon.tilfelleEndDate)
            it.setTimestamp(6, now)
            it.setTimestamp(7, now)
            it.setString(8, prediksjon.dataState)
            it.setFloat(9, prediksjon.prediksjonDelta)
            it.setString(10, forklaringRaw)
            it.setString(11, mapper.writeValueAsString(prediksjon.forklaring))
            it.executeQuery().toList { getInt("id") }
        }

        if (idList.size != 1) {
            throw SQLException("Creating prediksjonInput failed, no rows affected.")
        }
        connection.commit()

        return Pair(idList.first(), UUID.fromString(uuid))
    }
}
