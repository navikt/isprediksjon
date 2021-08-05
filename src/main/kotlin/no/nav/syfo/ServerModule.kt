package no.nav.syfo

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.routing.*
import no.nav.syfo.application.ApplicationState
import no.nav.syfo.application.api.registerPodApi
import no.nav.syfo.application.api.registerPrediksjon
import no.nav.syfo.application.installContentNegotiation
import no.nav.syfo.auth.*
import no.nav.syfo.clients.Tilgangskontroll
import no.nav.syfo.database.DatabaseInterface

fun Application.serverModule(
    applicationState: ApplicationState,
    database: DatabaseInterface,
    environment: Environment,
    wellKnownInternADV1: WellKnown,
) {
    log.info("Initialization of server module starting")

    installContentNegotiation()
    auth(
        jwtIssuerList = listOf(
            JwtIssuer(
                acceptedAudienceList = listOf(environment.loginserviceClientId),
                jwtIssuerType = JwtIssuerType.INTERN_AZUREAD_V1,
                wellKnown = wellKnownInternADV1,
            ),
        ),
    )

    routing {
        registerPodApi(applicationState)
        authenticate(JwtIssuerType.INTERN_AZUREAD_V1.name) {
            registerPrediksjon(
                database,
                Tilgangskontroll(environment.tilgangskontrollUrl),
                MidlertidigTilgangsSjekk(environment.tilgangPath),
            )
        }
    }
}
