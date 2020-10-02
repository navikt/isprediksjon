package no.nav.syfo.clients.syketilfelle

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import no.nav.syfo.clients.sts.StsRestClient
import no.nav.syfo.clients.syketilfelle.domain.KOppfolgingstilfellePerson
import no.nav.syfo.domain.AktorId
import no.nav.syfo.log
import no.nav.syfo.metric.COUNT_CALL_SYKETILFELLE_OPPFOLGINGSTILFELLE_AKTOR_EMPTY
import no.nav.syfo.metric.COUNT_CALL_SYKETILFELLE_OPPFOLGINGSTILFELLE_AKTOR_FAIL
import no.nav.syfo.metric.COUNT_CALL_SYKETILFELLE_OPPFOLGINGSTILFELLE_AKTOR_SUCCESS
import no.nav.syfo.util.APP_CONSUMER_ID
import no.nav.syfo.util.NAV_CALL_ID
import no.nav.syfo.util.NAV_CONSUMER_ID
import no.nav.syfo.util.bearerHeader

class SyketilfelleClient(
    private val baseUrl: String,
    private val stsRestClient: StsRestClient
) {
    private val client = HttpClient(CIO) {
        install(JsonFeature) {
            serializer = JacksonSerializer {
                registerKotlinModule()
                registerModule(JavaTimeModule())
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            }
        }
    }

    suspend fun getOppfolgingstilfelle(
        aktorId: AktorId,
        callId: String
    ): KOppfolgingstilfellePerson? {
        val bearer = stsRestClient.token()

        val response: HttpResponse = client.get(getSyfosyketilfelleUrl(aktorId)) {
            header(HttpHeaders.Authorization, bearerHeader(bearer))
            header(NAV_CALL_ID, callId)
            header(NAV_CONSUMER_ID, APP_CONSUMER_ID)
            accept(ContentType.Application.Json)
        }

        when (response.status) {
            HttpStatusCode.OK -> {
                COUNT_CALL_SYKETILFELLE_OPPFOLGINGSTILFELLE_AKTOR_SUCCESS.inc()
                return response.receive<KOppfolgingstilfellePerson>()
            }
            HttpStatusCode.NoContent -> {
                log.error("Syketilfelle returned HTTP-${response.status.value}: No Oppfolgingstilfelle was found for AktorId")
                COUNT_CALL_SYKETILFELLE_OPPFOLGINGSTILFELLE_AKTOR_EMPTY.inc()
                return null
            }
            else -> {
                COUNT_CALL_SYKETILFELLE_OPPFOLGINGSTILFELLE_AKTOR_FAIL.inc()
                val errorMessage =
                    "Error with responseCode=${response.status.value} for callId=$callId when requesting Oppfolgingstilfelle for aktorId from syfosyketilfelle"
                log.error(errorMessage)
                return null
            }
        }
    }

    private fun getSyfosyketilfelleUrl(aktorId: AktorId): String {
        return "$baseUrl/kafka/oppfolgingstilfelle/beregn/${aktorId.value}"
    }
}
