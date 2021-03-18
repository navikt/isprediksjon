package no.nav.syfo

import io.ktor.application.*
import io.ktor.routing.*
import no.nav.syfo.application.ApplicationState
import no.nav.syfo.application.api.registerNaisApi
import no.nav.syfo.application.api.registerPrediksjon
import no.nav.syfo.database.DatabaseInterface

fun Application.serverModule(
    applicationState: ApplicationState,
    db: DatabaseInterface,
    env: Environment
) {
    log.info("Initialization of server module starting")

    routing {
        registerNaisApi(applicationState)
        registerPrediksjon(db, env)
    }
    log.info("Initialization of server module done")
}
