package no.nav.syfo.oppfolgingstilfelle.domain

import no.nav.syfo.domain.AktorId
import no.nav.syfo.domain.Fodselsnummer
import java.time.LocalDate

data class PersonOppfolgingstilfelle(
    val fnr: Fodselsnummer,
    val aktorId: AktorId,
    val tilfelleStartDate: LocalDate,
    val tilfelleEndDate: LocalDate,
)
