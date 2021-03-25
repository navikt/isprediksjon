package no.nav.syfo.application.api

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.syfo.clients.Tilgangskontroll
import no.nav.syfo.database
import no.nav.syfo.domain.Fodselsnummer
import no.nav.syfo.prediksjon.getPrediksjon
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val log: Logger = LoggerFactory.getLogger("no.nav.syfo.application.api")

const val apiBasePath = "/api/v1"
const val apiPrediksjon = "/prediksjon"
const val NAV_PERSONIDENT_HEADER = "nav-personident"

fun Route.registerPrediksjon(tilgangskontroll: Tilgangskontroll) {

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
                    call.respond(pred)
                } else {
                    call.respond(HttpStatusCode.Forbidden)
                }
            } catch (e: IllegalArgumentException) {
                val illegalArgumentMessage = "Could not retrieve PrediksjonList for PersonIdent"
                log.warn("$illegalArgumentMessage: {}", e.message)
                call.respond(HttpStatusCode.BadRequest, e.message ?: illegalArgumentMessage)
            }
        }
    }
}
