package no.nav.syfo.prediksjon

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.syfo.application.database.DatabaseInterface
import no.nav.syfo.application.database.toList
import no.nav.syfo.domain.AktorId
import no.nav.syfo.domain.Fodselsnummer
import java.sql.ResultSet
import java.time.ZoneOffset

const val queryHentPrediksjon = "SELECT * FROM prediksjon_output WHERE fnr = ?"

fun ResultSet.toPrediksjon(): PrediksjonOutput {
    val mapper = jacksonObjectMapper()

    return PrediksjonOutput(
        Fodselsnummer(getString("fnr")),
        AktorId(getString("aktorid")),
        getTimestamp("tilfelle_start_date").toInstant().atOffset(ZoneOffset.UTC),
        getTimestamp("tilfelle_end_date").toInstant().atOffset(ZoneOffset.UTC),
        getTimestamp("prediksjon_created").toInstant().atOffset(ZoneOffset.UTC),
        getString("datastate"),
        getFloat("prediksjon_delta"),
        mapper.readValue(getString("forklaring_front_end"))
    )
}

fun DatabaseInterface.getPrediksjon(fnr: Fodselsnummer): List<PrediksjonOutput> {
    return connection.use { connection ->
        connection.prepareStatement(queryHentPrediksjon).use {
            it.setString(1, fnr.value)
            it.executeQuery().toList {
                toPrediksjon()
            }
        }
    }
}
