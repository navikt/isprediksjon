package no.nav.syfo.clients.syketilfelle

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.kittinunf.fuel.httpGet
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
    fun getOppfolgingstilfelle(
        aktorId: AktorId,
        callId: String
    ): KOppfolgingstilfellePerson? {
        val bearer = stsRestClient.token()

        val (_, response, result) = getSyfosyketilfelleUrl(aktorId).httpGet()
            .header(
                mapOf(
                    HttpHeaders.Authorization to bearerHeader(bearer),
                    HttpHeaders.Accept to "application/json",
                    NAV_CALL_ID to callId,
                    NAV_CONSUMER_ID to APP_CONSUMER_ID
                )
            )
            .responseString()

        result.fold(
            success = {
                val responseCode = response.statusCode == 204
                return if (responseCode) {
                    log.error("Syketilfelle returned HTTP-$responseCode: No Oppfolgingstilfelle was found for AktorId")
                    COUNT_CALL_SYKETILFELLE_OPPFOLGINGSTILFELLE_AKTOR_EMPTY.inc()
                    null
                } else {
                    COUNT_CALL_SYKETILFELLE_OPPFOLGINGSTILFELLE_AKTOR_SUCCESS.inc()
                    objectMapper.readValue<KOppfolgingstilfellePerson>(result.get())
                }
            },
            failure = {
                COUNT_CALL_SYKETILFELLE_OPPFOLGINGSTILFELLE_AKTOR_FAIL.inc()
                val exception = it.exception
                val errorMessage =
                    "Error with responseCode=${response.statusCode} for callId=$callId when requesting Oppfolgingstilfelle for Aktord from syfosyketilfelle: ${exception.message}"
                log.error(
                    errorMessage,
                    exception
                )
                return null
            }
        )
    }

    private fun getSyfosyketilfelleUrl(aktorId: AktorId): String {
        return "$baseUrl/kafka/oppfolgingstilfelle/beregn/${aktorId.value}"
    }

    private val objectMapper: ObjectMapper = ObjectMapper().apply {
        registerKotlinModule()
        registerModule(JavaTimeModule())
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }
}
