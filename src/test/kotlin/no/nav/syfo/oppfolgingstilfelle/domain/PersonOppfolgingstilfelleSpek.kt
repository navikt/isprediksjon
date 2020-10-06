package no.nav.syfo.oppfolgingstilfelle.domain

import io.ktor.util.*
import no.nav.syfo.clients.syketilfelle.domain.KOppfolgingstilfellePerson
import no.nav.syfo.clients.syketilfelle.domain.KSyketilfelledag
import no.nav.syfo.domain.AktorId
import no.nav.syfo.domain.Fodselsnummer
import org.amshove.kluent.shouldBeEqualTo
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import testutil.UserConstants.ARBEIDSTAKER_FNR
import testutil.generator.generateKOppfolgingstilfelle
import java.time.LocalDate

@InternalAPI
object PersonOppfolgingstilfelleSpek : Spek({

    describe("isCandidateForPrediction") {
        val firstCandidateDate = LocalDate.now().minusDays(DAYS_START_CANDIDATE)
        val lastCandidateDate = LocalDate.now()

        it("should return true if KOppfolgingstilfelle is candidate for Prediksjon") {
            val kOppfolgingstilfellePersonCandidate = generateKOppfolgingstilfelle.copy(
                tidslinje = listOf(
                    KSyketilfelledag(
                        firstCandidateDate,
                        null
                    ),
                    KSyketilfelledag(
                        lastCandidateDate,
                        null
                    )
                )
            )
            val personOppfolgingstilfelle = mapToPersonOppfolgingstilfelle(
                ARBEIDSTAKER_FNR,
                kOppfolgingstilfellePersonCandidate
            )
            val result = personOppfolgingstilfelle.isCandidateForPrediction()

            result shouldBeEqualTo true
        }

        it("should return false if days since start date of KOppfolgingstilfelle is less than $DAYS_START_CANDIDATE") {
            val kOppfolgingstilfellePersonCandidate = generateKOppfolgingstilfelle.copy(
                tidslinje = listOf(
                    KSyketilfelledag(
                        firstCandidateDate.plusDays(1),
                        null
                    ),
                    KSyketilfelledag(
                        lastCandidateDate,
                        null
                    )
                )
            )
            val personOppfolgingstilfelle = mapToPersonOppfolgingstilfelle(
                ARBEIDSTAKER_FNR,
                kOppfolgingstilfellePersonCandidate
            )
            val result = personOppfolgingstilfelle.isCandidateForPrediction()

            result shouldBeEqualTo false
        }

        it("should return false if end date of KOppfolgingstilfelle is passed") {
            val kOppfolgingstilfellePersonCandidate = generateKOppfolgingstilfelle.copy(
                tidslinje = listOf(
                    KSyketilfelledag(
                        firstCandidateDate,
                        null
                    ),
                    KSyketilfelledag(
                        lastCandidateDate.minusDays(1),
                        null
                    )
                )
            )
            val personOppfolgingstilfelle = mapToPersonOppfolgingstilfelle(
                ARBEIDSTAKER_FNR,
                kOppfolgingstilfellePersonCandidate
            )
            val result = personOppfolgingstilfelle.isCandidateForPrediction()

            result shouldBeEqualTo false
        }
    }
})

fun mapToPersonOppfolgingstilfelle(
    fodselsnummer: Fodselsnummer,
    kOppfolgingstilfellePerson: KOppfolgingstilfellePerson
): PersonOppfolgingstilfelle {
    return PersonOppfolgingstilfelle(
        fnr = fodselsnummer,
        aktorId = AktorId(kOppfolgingstilfellePerson.aktorId),
        tilfelleStartDate = kOppfolgingstilfellePerson.tidslinje.first().dag,
        tilfelleEndDate = kOppfolgingstilfellePerson.tidslinje.last().dag
    )
}
