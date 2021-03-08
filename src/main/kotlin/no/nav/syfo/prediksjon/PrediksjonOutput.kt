package no.nav.syfo.prediksjon

import no.nav.syfo.domain.AktorId
import no.nav.syfo.domain.Fodselsnummer
import java.time.OffsetDateTime

data class ForklaringFrontend(val ned: List<String>, val opp: List<String>)

data class Prediksjon(
    val fnr: Fodselsnummer,
    val aktorId: AktorId,
    val tilfelleStartDate: OffsetDateTime,
    val tilfelleEndDate: OffsetDateTime,
    val dataState: String,
    val prediksjonDelta: Float,
    val forklaring: ForklaringFrontend
)
