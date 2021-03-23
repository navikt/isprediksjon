package no.nav.syfo

import io.ktor.application.*
import io.ktor.routing.*
import no.nav.syfo.application.ApplicationState
import no.nav.syfo.application.api.registerNaisApi
import no.nav.syfo.application.api.registerPrediksjon
import no.nav.syfo.application.installContentNegotiation
import no.nav.syfo.clients.Tilgangskontroll

fun Application.serverModule(
    applicationState: ApplicationState,
    env: Environment
) {
    log.info("Initialization of server module starting")

    installContentNegotiation()

    routing {
        registerNaisApi(applicationState)
        registerPrediksjon(Tilgangskontroll(env.developmentMode))
    }
    log.info("Initialization of server module done")
}
