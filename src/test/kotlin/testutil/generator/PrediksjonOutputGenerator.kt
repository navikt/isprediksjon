package testutil.generator

import no.nav.syfo.domain.AktorId
import no.nav.syfo.domain.Fodselsnummer
import no.nav.syfo.prediksjon.DAYS_IN_LONG_FRAVAR
import no.nav.syfo.prediksjon.ForklaringFrontend
import no.nav.syfo.prediksjon.PrediksjonOutput
import testutil.UserConstants.ARBEIDSTAKER_AKTORID
import testutil.UserConstants.ARBEIDSTAKER_FNR
import java.time.OffsetDateTime

val shortFravarDays = DAYS_IN_LONG_FRAVAR - 30
val oldPrediksjonCreated: OffsetDateTime = OffsetDateTime.now().minusDays(60)

fun generatePrediksjonOutput(
    fnr: Fodselsnummer = ARBEIDSTAKER_FNR,
    aktorId: AktorId = ARBEIDSTAKER_AKTORID,
    tilfelleStartDate: OffsetDateTime = OffsetDateTime.now(),
    tilfelleEndDate: OffsetDateTime = OffsetDateTime.now(),
    prediksjonCreated: OffsetDateTime = OffsetDateTime.now(),
    dataState: String = "OK",
    prediksjonDelta: Float = 0.95f,
    forklaring: ForklaringFrontend = ForklaringFrontend(listOf("diagnosis", "md"), listOf("grad", "time", "hist"))
): PrediksjonOutput {
    return PrediksjonOutput(
        fnr = fnr,
        aktorId = aktorId,
        tilfelleStartDate = tilfelleStartDate,
        tilfelleEndDate = tilfelleEndDate,
        prediksjonCreated = prediksjonCreated,
        dataState = dataState,
        prediksjonDelta = prediksjonDelta,
        forklaring = forklaring
    )
}

val generateGenericPrediksjonOutput = generatePrediksjonOutput()

val generatePrediksjonOutputLong =
    generatePrediksjonOutput(prediksjonDelta = DAYS_IN_LONG_FRAVAR.toFloat())

val generatePrediksjonOutputShort =
    generatePrediksjonOutput(prediksjonDelta = shortFravarDays.toFloat())

val generateOldPrediksjonOutput = generatePrediksjonOutput(prediksjonCreated = oldPrediksjonCreated)
