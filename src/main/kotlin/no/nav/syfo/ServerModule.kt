package no.nav.syfo

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.routing.*
import no.nav.syfo.application.ApplicationState
import no.nav.syfo.application.api.registerNaisApi
import no.nav.syfo.application.api.registerPrediksjon
import no.nav.syfo.application.installContentNegotiation
import no.nav.syfo.auth.WellKnown
import no.nav.syfo.auth.auth
import no.nav.syfo.clients.Tilgangskontroll
import no.nav.syfo.database.DatabaseInterface
import no.nav.syfo.auth.MidlertidigTilgangsSjekk

fun Application.serverModule(
    applicationState: ApplicationState,
    database: DatabaseInterface,
    env: Environment,
    wellKnown: WellKnown
) {
    log.info("Initialization of server module starting")

    installContentNegotiation()
    auth(wellKnown, listOf(env.loginserviceClientId))

    routing {
        registerNaisApi(applicationState)
        authenticate {
            registerPrediksjon(
                database,
                Tilgangskontroll(env.tilgangskontrollUrl),
                MidlertidigTilgangsSjekk(env.tilgangPath)
            )
        }
    }
    log.info("Initialization of server module done")
}
