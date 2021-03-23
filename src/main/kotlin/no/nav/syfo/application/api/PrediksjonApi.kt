package no.nav.syfo.application.api

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.syfo.clients.Tilgangskontroll
import no.nav.syfo.database
import no.nav.syfo.domain.Fodselsnummer
import no.nav.syfo.prediksjon.getPrediksjon
import org.slf4j.LoggerFactory
import java.time.OffsetDateTime

private val log = LoggerFactory.getLogger("no.nav.syfo.application.api.PrediksjonApiKt")

const val apiBasePath = "/api/v1"
const val apiPrediksjon = "/prediksjon"
const val NAV_PERSONIDENT_HEADER = "nav-personident"

fun Route.registerPrediksjon(tilgangskontroll: Tilgangskontroll) {

    route(apiBasePath) {
        get(apiPrediksjon) {
            try {
                log.info("L-TRACE: GET Prediksjon!")
                val token = call.request.headers[HttpHeaders.Authorization]?.removePrefix("Bearer ")
                    ?: throw IllegalArgumentException("No Authorization header supplied")
                log.info("L-TRACE: Fikk token!")

                val requestFnr =
                    call.request.headers[NAV_PERSONIDENT_HEADER] ?: throw IllegalArgumentException("No Fnr supplied")
                log.info("L-TRACE: Fikk fnr")

                val tilgang = tilgangskontroll.harTilgangTilBruker(Fodselsnummer(requestFnr), token)

                if (tilgang) {
                    log.info("L-TRACE: Har tilgang")
                    val pred = database.getPrediksjon(Fodselsnummer(requestFnr))
                    log.info("L-TRACE: Returnerer prediksjon!")
                    call.respond(pred)
                } else {
                    log.info("L-TRACE: Har ikke tilgang!")
                    call.respond(HttpStatusCode.Forbidden)
                }
            } catch (e: Exception) {
                log.info("L-TRACE: Fikk en exception! :O :( ", e)
                throw e
            }
        }
    }
}
