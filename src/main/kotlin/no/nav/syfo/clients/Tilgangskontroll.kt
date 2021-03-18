package no.nav.syfo.clients

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import net.logstash.logback.argument.StructuredArguments
import no.nav.syfo.domain.Fodselsnummer
import no.nav.syfo.util.bearerHeader
import org.slf4j.LoggerFactory

class Tilgangskontroll(isDev: Boolean) {

    val domain = if (isDev) "nais.preprod.local" else "nais.adeo.no"
    private val url: String =
        "http://syfo-tilgangskontroll.$domain/syfo-tilgangskontroll/api/tilgang/bruker"

    data class Tilgang(val harTilgang: Boolean, val begrunnelse: String? = null)

    private val log = LoggerFactory.getLogger("no.nav.isprediksjon.clients.Tilgangskontroll")

    private val httpClient = HttpClient(CIO) {
        install(JsonFeature) {
            serializer = JacksonSerializer()
        }
    }

    suspend fun harTilgangTilBruker(fnr: Fodselsnummer, token: String): Boolean {
        try {
            val tilgang = httpClient.get<Tilgang>(url) {
                url("$url?fnr=${fnr.value}")
                header(HttpHeaders.Authorization, bearerHeader(token))
                accept(ContentType.Application.Json)
            }

            return tilgang.harTilgang
        } catch (e: ClientRequestException) {
            return if (e.response.status == HttpStatusCode.Forbidden) {
                false
            } else {
                return handleUnexpectedReponseException(e.response)
            }
        } catch (e: ServerResponseException) {
            return handleUnexpectedReponseException(e.response)
        }
    }

    private fun handleUnexpectedReponseException(response: HttpResponse): Boolean {
        val statusCode = response.status.value.toString()
        log.error(
            "Error while requesting access to person from syfo-tilgangskontroll with {}",
            StructuredArguments.keyValue("statusCode", statusCode)
        )
        return false
    }
}
