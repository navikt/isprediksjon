package no.nav.syfo.prediksjon

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.syfo.database.DatabaseInterface
import no.nav.syfo.database.toList
import no.nav.syfo.domain.AktorId
import no.nav.syfo.domain.Fodselsnummer
import java.sql.ResultSet
import java.time.ZoneOffset

const val queryHentPrediksjon = "SELECT * FROM prediksjon_output WHERE fnr = ?"

fun ResultSet.toPrediksjon(): Prediksjon {
    val mapper = jacksonObjectMapper()

    return Prediksjon(
        Fodselsnummer(getString("fnr")),
        AktorId(getString("aktorid")),
        getTimestamp("tilfelle_start_date").toInstant().atOffset(ZoneOffset.UTC),
        getTimestamp("tilfelle_end_date").toInstant().atOffset(ZoneOffset.UTC),
        getString("datastate"),
        getFloat("prediksjon_delta"),
        mapper.readValue(getString("forklaring_front_end"))
    )
}

fun DatabaseInterface.getPrediksjon(fnr: Fodselsnummer): List<Prediksjon> {
    return connection.use {
        connection.prepareStatement(queryHentPrediksjon).use {
            it.setString(1, fnr.value)
            it.executeQuery().toList {
                toPrediksjon()
            }
        }
    }
}
