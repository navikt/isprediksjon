package testutil.generator

import no.nav.syfo.prediksjon.ForklaringFrontend
import no.nav.syfo.prediksjon.PrediksjonOutput
import testutil.UserConstants.ARBEIDSTAKER_AKTORID
import testutil.UserConstants.ARBEIDSTAKER_FNR
import java.time.OffsetDateTime

val generatePrediksjonOutput =
    PrediksjonOutput(
        ARBEIDSTAKER_FNR,
        ARBEIDSTAKER_AKTORID,
        OffsetDateTime.now(),
        OffsetDateTime.now(),
        OffsetDateTime.now(),
        "OK",
        0.95f,
        ForklaringFrontend(listOf("diagnosis", "md"), listOf("grad", "time", "hist"))
    )
