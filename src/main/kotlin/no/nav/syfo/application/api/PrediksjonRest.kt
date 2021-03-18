package no.nav.syfo.application.api

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.syfo.Environment
import no.nav.syfo.clients.Tilgangskontroll
import no.nav.syfo.database.DatabaseInterface
import no.nav.syfo.domain.AktorId
import no.nav.syfo.domain.Fodselsnummer
import no.nav.syfo.prediksjon.getPrediksjon

const val apiBasePath = "/api/v1"
const val apiPrediksjon = "/prediksjon"

fun Route.registerPrediksjon(database: DatabaseInterface, tilgangskontroll: Tilgangskontroll) {
    route(apiBasePath) {
        get(apiPrediksjon) {
            val token = call.request.headers[HttpHeaders.Authorization]?.removePrefix("Bearer ")
                ?: throw IllegalArgumentException("No Authorization header supplied")

            val requestFnr = call.request.headers["fnr"] ?: throw IllegalArgumentException("No Fnr supplied")
            val requestAktorid =
                call.request.headers["aktorid"] ?: throw IllegalArgumentException("No AktorId supplied")

            val tilgang = tilgangskontroll.harTilgangTilBruker(Fodselsnummer(requestFnr), token)

            if (tilgang) {
                val pred = database.getPrediksjon(Fodselsnummer(requestFnr), AktorId(requestAktorid))
                call.respond(pred)
            } else {
                call.respond(HttpStatusCode.Forbidden)
            }
        }
    }
}
