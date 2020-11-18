package no.nav.syfo.clients.aktor

import no.nav.syfo.domain.AktorId
import no.nav.syfo.log

class AktorService(
    private val aktorregisterClient: AktorregisterClient
) {

    suspend fun fodselsnummerForAktor(aktorId: AktorId, callId: String): String? {
        var fnr: String? = null
        aktorregisterClient.getNorskIdent(aktorId.value, callId).mapLeft {
            if (it == "Den angitte personidenten finnes ikke") return null // We got a response with no matches

            val message = "Did not find Fodelsnummer for AktorId" // Call failed for some other reason
            log.error(message)
            throw IllegalStateException(message)
        }.map {
            fnr = it
        }
        return fnr
    }
}
