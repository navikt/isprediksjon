package no.nav.syfo.prediksjon.input

import java.time.LocalDate
import java.time.LocalDateTime

data class PPrediksjonInput(
    val id: Int,
    val uuid: String,
    val fnr: String,
    val aktorId: String,
    val tilfelleStartDate: LocalDate,
    val tilfelleEndDate: LocalDate,
    val created: LocalDateTime,
)
