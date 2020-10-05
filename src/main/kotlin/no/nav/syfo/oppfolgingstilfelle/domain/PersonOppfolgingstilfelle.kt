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

const val WEEKS_START_CANDIDATE = 16
const val DAYS_START_CANDIDATE = WEEKS_START_CANDIDATE * 7L

fun PersonOppfolgingstilfelle.isCandidateForPrediction(): Boolean {
    val todayDate = LocalDate.now()
    val startDate = this.tilfelleStartDate
    val endDate = this.tilfelleEndDate
    val candidateDate = startDate.plusDays(DAYS_START_CANDIDATE)

    val isCandidateDatePassed = todayDate.isAfter(candidateDate.minusDays(1))
    val isCandidateSickToday = todayDate.isAfter(startDate.minusDays(1)) && todayDate.isBefore(endDate.plusDays(1))

    return isCandidateDatePassed && isCandidateSickToday
}
