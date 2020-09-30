package testutil.generator

import no.nav.syfo.oppfolgingstilfelle.domain.PersonOppfolgingstilfelle
import testutil.UserConstants.ARBEIDSTAKER_AKTORID
import testutil.UserConstants.ARBEIDSTAKER_FNR
import java.time.LocalDate

val generatePersonOppfolgingstilfelle =
    PersonOppfolgingstilfelle(
        fnr = ARBEIDSTAKER_FNR,
        aktorId = ARBEIDSTAKER_AKTORID,
        tilfelleStartDate = LocalDate.now().minusDays(112),
        tilfelleEndDate = LocalDate.now().plusDays(10)
    )
