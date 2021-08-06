package no.nav.syfo.application.api

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.syfo.tilgangskontroll.MidlertidigTilgangsSjekk
import no.nav.syfo.application.api.authentication.getNAVIdentFromToken
import no.nav.syfo.clients.tilgangskontroll.Tilgangskontroll
import no.nav.syfo.database.DatabaseInterface
import no.nav.syfo.domain.Fodselsnummer
import no.nav.syfo.metric.COUNT_PREDIKSJON_OUTPUT_ERROR
import no.nav.syfo.metric.COUNT_PREDIKSJON_OUTPUT_FAILED
import no.nav.syfo.metric.COUNT_PREDIKSJON_OUTPUT_FORBIDDEN
import no.nav.syfo.metric.COUNT_PREDIKSJON_OUTPUT_SUCCESS
import no.nav.syfo.prediksjon.getPrediksjon
import no.nav.syfo.prediksjon.toPrediksjonFrontend
import no.nav.syfo.util.getCallId
import no.nav.syfo.util.latestPrediksjon
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val log: Logger = LoggerFactory.getLogger("no.nav.syfo.application.api")

const val apiV2BasePath = "/api/v2"
const val apiV2PrediksjonPath = "/prediksjon"

fun Route.registerPrediksjonApiV2(
    database: DatabaseInterface,
    tilgangskontroll: Tilgangskontroll,
    midlertidigTilgangsSjekk: MidlertidigTilgangsSjekk,
) {
    route(apiV2BasePath) {
        get(apiV2PrediksjonPath) {
            val callId = getCallId()
            try {
                val token = call.request.headers[HttpHeaders.Authorization]?.removePrefix("Bearer ")
                    ?: throw IllegalArgumentException("No Authorization header supplied")

                val requestFnr =
                    call.request.headers[NAV_PERSONIDENT_HEADER]
                        ?: throw IllegalArgumentException("No PersonIdent supplied")

                val tilgang = tilgangskontroll.hasAccessWithOBO(
                    callId = callId,
                    personIdentNumber = Fodselsnummer(requestFnr),
                    token = token,
                )

                val veilederHasBetaAccess = midlertidigTilgangsSjekk.harTilgang(
                    navIdent = getNAVIdentFromToken(token),
                )

                if (tilgang && veilederHasBetaAccess) {
                    val pred = database.getPrediksjon(Fodselsnummer(requestFnr))

                    if (pred.isEmpty()) {
                        call.respond(HttpStatusCode.NoContent)
                    } else {
                        val latestPrediksjon = pred.latestPrediksjon()
                        val frontendPrediksjon = latestPrediksjon.toPrediksjonFrontend()

                        call.respond(frontendPrediksjon)
                    }
                    COUNT_PREDIKSJON_OUTPUT_SUCCESS.inc()
                } else {
                    COUNT_PREDIKSJON_OUTPUT_FORBIDDEN.inc()
                    call.respond(HttpStatusCode.Forbidden)
                }
            } catch (e: IllegalArgumentException) {
                val illegalArgumentMessage = "Could not retrieve PrediksjonList for PersonIdent"
                log.error("$illegalArgumentMessage: {}", e.message)
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
