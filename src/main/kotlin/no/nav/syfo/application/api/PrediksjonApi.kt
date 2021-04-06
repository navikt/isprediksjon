package no.nav.syfo.application.api

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.syfo.clients.Tilgangskontroll
import no.nav.syfo.database.DatabaseInterface
import no.nav.syfo.domain.Fodselsnummer
import no.nav.syfo.metric.COUNT_PREDIKSJON_OUTPUT_ERROR
import no.nav.syfo.metric.COUNT_PREDIKSJON_OUTPUT_FAILED
import no.nav.syfo.metric.COUNT_PREDIKSJON_OUTPUT_FORBIDDEN
import no.nav.syfo.metric.COUNT_PREDIKSJON_OUTPUT_SUCCESS
import no.nav.syfo.prediksjon.getPrediksjon
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val log: Logger = LoggerFactory.getLogger("no.nav.syfo.application.api")

const val apiBasePath = "/api/v1"
const val apiPrediksjon = "/prediksjon"
const val NAV_PERSONIDENT_HEADER = "nav-personident"

fun Route.registerPrediksjon(
    database: DatabaseInterface,
    tilgangskontroll: Tilgangskontroll
) {

    route(apiBasePath) {
        get(apiPrediksjon) {
            try {
                val token = call.request.headers[HttpHeaders.Authorization]?.removePrefix("Bearer ")
                    ?: throw IllegalArgumentException("No Authorization header supplied")

                val requestFnr =
                    call.request.headers[NAV_PERSONIDENT_HEADER] ?: throw IllegalArgumentException("No Fnr supplied")

                val tilgang = tilgangskontroll.harTilgangTilBruker(Fodselsnummer(requestFnr), token)

                if (tilgang) {
                    val pred = database.getPrediksjon(Fodselsnummer(requestFnr))
                    COUNT_PREDIKSJON_OUTPUT_SUCCESS.inc()
                    call.respond(pred)
                } else {
                    COUNT_PREDIKSJON_OUTPUT_FORBIDDEN.inc()
                    call.respond(HttpStatusCode.Forbidden)
                }
            } catch (e: IllegalArgumentException) {
                val illegalArgumentMessage = "Could not retrieve PrediksjonList for PersonIdent"
                log.warn("$illegalArgumentMessage: {}", e.message)
                COUNT_PREDIKSJON_OUTPUT_FAILED.inc()
                call.respond(HttpStatusCode.BadRequest, e.message ?: illegalArgumentMessage)
            } catch (e: Exception) {
                val errorMessage = "An unexpected exception occurred"
                log.error(errorMessage, e)
                COUNT_PREDIKSJON_OUTPUT_ERROR.inc()
                call.respond(HttpStatusCode.InternalServerError, errorMessage)
            }
        }
    }
}
