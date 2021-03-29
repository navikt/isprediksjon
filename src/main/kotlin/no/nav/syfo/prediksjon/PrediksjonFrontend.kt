package no.nav.syfo.prediksjon

import java.time.Duration
import java.time.OffsetDateTime

val WEEKS_IN_LONG_FRAVAR = 28
val DAYS_IN_LONG_FRAVAR = WEEKS_IN_LONG_FRAVAR * 7

data class PrediksjonFrontend(
    val kortereVarighetGrunner: List<String>,
    val langt: Boolean,
    val lengreVarighetGrunner: List<String>,
    val prediksjonsDato: OffsetDateTime
)

fun PrediksjonOutput.toPrediksjonFrontend(): PrediksjonFrontend {
    return PrediksjonFrontend(
        kortereVarighetGrunner = forklaring.ned,
        langt = isLongSykefravar(),
        lengreVarighetGrunner = forklaring.opp,
        prediksjonsDato = prediksjonCreated
    )
}

fun PrediksjonOutput.isLongSykefravar(): Boolean {
    val predictedEndDate = prediksjonCreated.plusDays(prediksjonDelta.toLong())
    val expectedDaysInFravar = Duration.between(tilfelleStartDate, predictedEndDate)

    return expectedDaysInFravar >= Duration.ofDays(DAYS_IN_LONG_FRAVAR.toLong())
}
