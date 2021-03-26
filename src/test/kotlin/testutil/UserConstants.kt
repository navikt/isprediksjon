package testutil

import no.nav.syfo.domain.AktorId
import no.nav.syfo.domain.Fodselsnummer

object UserConstants {
    val ARBEIDSTAKER_FNR = Fodselsnummer("12345678912")
    val ARBEIDSTAKER_VEILEDER_NO_ACCESS = Fodselsnummer(ARBEIDSTAKER_FNR.value.replace("2", "1"))
    val ARBEIDSTAKER_AKTORID = AktorId("1234567891201")
    val ARBEIDSTAKER_AKTORID_FINNES_IKKE = AktorId("1234567891202")
    const val VIRKSOMHETSNUMMER = "123456789"
}
