package testutil.generator

import no.nav.syfo.prediksjon.input.PPrediksjonInput
import testutil.UserConstants.ARBEIDSTAKER_AKTORID
import testutil.UserConstants.ARBEIDSTAKER_FNR
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

val generatePPrediksjonInput =
    PPrediksjonInput(
        id = 1,
        uuid = UUID.randomUUID().toString(),
        fnr = ARBEIDSTAKER_FNR.value,
        aktorId = ARBEIDSTAKER_AKTORID.value,
        tilfelleStartDate = LocalDate.now().minusDays(112),
        tilfelleEndDate = LocalDate.now().plusDays(10),
        created = LocalDateTime.now()
    )
