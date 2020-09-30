package no.nav.syfo.oppfolgingstilfelle

import no.nav.syfo.clients.aktor.AktorService
import no.nav.syfo.clients.syketilfelle.SyketilfelleClient
import no.nav.syfo.domain.AktorId
import no.nav.syfo.domain.Fodselsnummer
import no.nav.syfo.log
import no.nav.syfo.metric.COUNT_OPPFOLGINGSTILFELLE_SKIPPED_FODSELSNUMMER
import no.nav.syfo.metric.COUNT_OPPFOLGINGSTILFELLE_SKIPPED_OPPFOLGINGSTILFELLE
import no.nav.syfo.metric.COUNT_PREDIKSJON_INPUT_CREATED
import no.nav.syfo.oppfolgingstilfelle.domain.KOppfolgingstilfellePeker
import no.nav.syfo.oppfolgingstilfelle.domain.PersonOppfolgingstilfelle
import no.nav.syfo.prediksjon.PrediksjonInputService

class OppfolgingstilfelleService(
    private val aktorService: AktorService,
    private val prediksjonInputService: PrediksjonInputService,
    private val syketilfelleClient: SyketilfelleClient
) {
    fun receiveOppfolgingstilfelle(
        oppfolgingstilfellePeker: KOppfolgingstilfellePeker,
        callId: String = ""
    ) {
        val aktor = AktorId(oppfolgingstilfellePeker.aktorId)

        val fnr: String = aktorService.fodselsnummerForAktor(aktor, callId)
            ?: return skipOppfolgingstilfelleWithMissingValue(MissingValue.FODSELSNUMMER)

        val oppfolgingstilfelle = syketilfelleClient.getOppfolgingstilfelle(
            AktorId(oppfolgingstilfellePeker.aktorId),
            callId
        )

        oppfolgingstilfelle?.let {
            val personOppfolgingstilfelle = PersonOppfolgingstilfelle(
                fnr = Fodselsnummer(fnr),
                aktorId = AktorId(it.aktorId),
                tilfelleStartDate = it.tidslinje.first().dag,
                tilfelleEndDate = it.tidslinje.last().dag
            )
            prediksjonInputService.createPrediksjonInput(personOppfolgingstilfelle)
            COUNT_PREDIKSJON_INPUT_CREATED.inc()
        } ?: return skipOppfolgingstilfelleWithMissingValue(MissingValue.OPPFOLGINGSTILFELLE)
    }

    private fun skipOppfolgingstilfelleWithMissingValue(missingValue: MissingValue) {
        log.error("Skipping Oppfolgingstilfelle due to missing value ${missingValue.name}")
        when (missingValue) {
            MissingValue.FODSELSNUMMER -> COUNT_OPPFOLGINGSTILFELLE_SKIPPED_FODSELSNUMMER.inc()
            MissingValue.OPPFOLGINGSTILFELLE -> COUNT_OPPFOLGINGSTILFELLE_SKIPPED_OPPFOLGINGSTILFELLE.inc()
        }
    }
}

enum class MissingValue {
    FODSELSNUMMER,
    OPPFOLGINGSTILFELLE
}
