package no.nav.syfo

import io.ktor.application.*
import io.ktor.routing.*
import no.nav.syfo.application.ApplicationState
import no.nav.syfo.application.api.registerNaisApi
import no.nav.syfo.application.api.registerPrediksjon
import no.nav.syfo.application.installContentNegotiation
import no.nav.syfo.clients.Tilgangskontroll
import no.nav.syfo.database.DatabaseInterface

fun Application.serverModule(
    applicationState: ApplicationState,
    database: DatabaseInterface,
    env: Environment
) {
    log.info("Initialization of server module starting")

    installContentNegotiation()

    routing {
        registerNaisApi(applicationState)
        registerPrediksjon(
            database,
            Tilgangskontroll(env.tilgangskontrollUrl)
        )
    }
    log.info("Initialization of server module done")
}
