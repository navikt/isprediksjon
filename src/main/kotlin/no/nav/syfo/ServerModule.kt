package no.nav.syfo

import io.ktor.application.*
import io.ktor.routing.*
import no.nav.syfo.application.ApplicationState
import no.nav.syfo.application.api.registerNaisApi

fun Application.serverModule(
    applicationState: ApplicationState
) {
    routing {
        registerNaisApi(applicationState)
    }
}