package testutil.generator

import no.nav.syfo.clients.syketilfelle.domain.KOppfolgingstilfellePerson
import no.nav.syfo.clients.syketilfelle.domain.KSyketilfelledag
import no.nav.syfo.oppfolgingstilfelle.domain.DAYS_START_CANDIDATE
import no.nav.syfo.oppfolgingstilfelle.domain.KOppfolgingstilfellePeker
import testutil.UserConstants.ARBEIDSTAKER_AKTORID
import testutil.UserConstants.VIRKSOMHETSNUMMER
import java.time.LocalDate
import java.time.LocalDateTime

val generateKOppfolgingstilfellePeker =
    KOppfolgingstilfellePeker(
        aktorId = ARBEIDSTAKER_AKTORID.value,
        orgnummer = VIRKSOMHETSNUMMER
    ).copy()

val generateKOppfolgingstilfelle =
    KOppfolgingstilfellePerson(
        ARBEIDSTAKER_AKTORID.value,
        listOf(
            KSyketilfelledag(
                LocalDate.now().minusDays(DAYS_START_CANDIDATE),
                null
            ),
            KSyketilfelledag(
                LocalDate.now().plusDays(10),
                null
            )
        ),
        KSyketilfelledag(
            LocalDate.now().minusDays(1),
            null
        ),
        0,
        false,
        LocalDateTime.now()
    ).copy()
