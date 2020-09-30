package no.nav.syfo.clients.aktor

import no.nav.syfo.domain.AktorId
import no.nav.syfo.log

class AktorService(
    private val aktorregisterClient: AktorregisterClient
) {
    fun getFodselsnummerForAktor(aktorId: AktorId, callId: String) =
        aktorregisterClient.getNorskIdent(aktorId.value, callId).mapLeft {
            throw IllegalStateException("Did not find Aktor")
        }

    fun fodselsnummerForAktor(aktorId: AktorId, callId: String): String? {
        var fnr: String? = null
        getFodselsnummerForAktor(aktorId, callId).mapLeft {
            val message = "Did not find Fodelsnummer for AktorId"
            log.error(message)
            throw IllegalStateException(message)
        }.map {
            fnr = it
        }
        return fnr
    }
}
