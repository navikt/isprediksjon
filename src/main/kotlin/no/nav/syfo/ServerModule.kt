package no.nav.syfo

import io.ktor.application.*
import io.ktor.routing.*
import no.nav.syfo.application.ApplicationState
import no.nav.syfo.application.api.registerNaisApi

fun Application.serverModule(
    applicationState: ApplicationState
) {
    log.info("Initialization of server module starting")

    routing {
        registerNaisApi(applicationState)
    }
    log.info("Initialization of server module done")
}
