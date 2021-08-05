package no.nav.syfo.clients

import io.ktor.client.call.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import net.logstash.logback.argument.StructuredArguments
import no.nav.syfo.clients.azuread.v2.AzureAdV2Client
import no.nav.syfo.domain.Fodselsnummer
import no.nav.syfo.metric.COUNT_TILGANGSKONTROLL_FAIL
import no.nav.syfo.metric.COUNT_TILGANGSKONTROLL_FORBIDDEN
import no.nav.syfo.metric.COUNT_TILGANGSKONTROLL_OK
import no.nav.syfo.util.NAV_CALL_ID
import no.nav.syfo.util.bearerHeader
import org.slf4j.LoggerFactory

class Tilgangskontroll(
    private val azureAdV2Client: AzureAdV2Client,
    private val baseUrl: String,
    private val syfotilgangskontrollClientId: String,
) {

    val url = "$baseUrl/syfo-tilgangskontroll/api/tilgang/bruker"

    data class Tilgang(
        val harTilgang: Boolean,
        val begrunnelse: String? = null,
    )

    private val httpClient = httpClientDefault()

    suspend fun harTilgangTilBruker(fnr: Fodselsnummer, token: String): Boolean {
        try {
            val completeUrl = "$url?fnr=${fnr.value}"
            val tilgang = httpClient.get<Tilgang>(completeUrl) {
                header(HttpHeaders.Authorization, bearerHeader(token))
                accept(ContentType.Application.Json)
            }
            COUNT_TILGANGSKONTROLL_OK.inc()

            return tilgang.harTilgang
        } catch (e: ClientRequestException) {
            return if (e.response.status == HttpStatusCode.Forbidden) {
                COUNT_TILGANGSKONTROLL_FORBIDDEN.inc()
                false
            } else {
                return handleUnexpectedReponseException(e.response)
            }
        } catch (e: ServerResponseException) {
            return handleUnexpectedReponseException(e.response)
        }
    }

    suspend fun hasAccessWithOBO(
        callId: String,
        personIdentNumber: Fodselsnummer,
        token: String,
    ): Boolean {
        val oboToken = azureAdV2Client.getOnBehalfOfToken(
            scopeClientId = syfotilgangskontrollClientId,
            token = token,
        )?.accessToken ?: throw RuntimeException("Failed to request access to Person: Failed to get OBO token")

        try {
            val url = getTilgangskontrollV2Url(personIdentNumber)
            val response: HttpResponse = httpClient.get(url) {
                header(HttpHeaders.Authorization, bearerHeader(oboToken))
                header(NAV_CALL_ID, callId)
                accept(ContentType.Application.Json)
            }
            COUNT_TILGANGSKONTROLL_OK.inc()
            return response.receive<Tilgang>().harTilgang
        } catch (e: ClientRequestException) {
            return if (e.response.status == HttpStatusCode.Forbidden) {
                COUNT_TILGANGSKONTROLL_FORBIDDEN.inc()
                false
            } else {
                return handleUnexpectedReponseException(e.response)
            }
        } catch (e: ServerResponseException) {
            return handleUnexpectedReponseException(e.response)
        }
    }

    private fun getTilgangskontrollV2Url(personIdentNumber: Fodselsnummer): String {
        return "$baseUrl$TILGANGSKONTROLL_V2_PERSON_PATH/${personIdentNumber.value}"
    }

    private fun handleUnexpectedReponseException(response: HttpResponse): Boolean {
        val statusCode = response.status.value.toString()
        log.error(
            "Error while requesting access to person from syfo-tilgangskontroll with {}",
            StructuredArguments.keyValue("statusCode", statusCode)
        )
        COUNT_TILGANGSKONTROLL_FAIL.labels(statusCode).inc()
        return false
    }

    companion object {
        private val log = LoggerFactory.getLogger(Tilgangskontroll::class.java)
        const val TILGANGSKONTROLL_V2_PERSON_PATH = "/syfo-tilgangskontroll/api/tilgang/navident/bruker"
    }
}
