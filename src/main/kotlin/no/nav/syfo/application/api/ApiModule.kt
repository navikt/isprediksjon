package no.nav.syfo.application.api

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.routing.*
import no.nav.syfo.Environment
import no.nav.syfo.application.ApplicationState
import no.nav.syfo.application.installContentNegotiation
import no.nav.syfo.auth.*
import no.nav.syfo.clients.Tilgangskontroll
import no.nav.syfo.clients.azuread.v2.AzureAdV2Client
import no.nav.syfo.database.DatabaseInterface

fun Application.apiModule(
    applicationState: ApplicationState,
    database: DatabaseInterface,
    environment: Environment,
    wellKnownInternADV1: WellKnown,
    wellKnownInternADV2: WellKnown,
) {
    installContentNegotiation()
    auth(
        jwtIssuerList = listOf(
            JwtIssuer(
                acceptedAudienceList = listOf(environment.loginserviceClientId),
                jwtIssuerType = JwtIssuerType.INTERN_AZUREAD_V1,
                wellKnown = wellKnownInternADV1,
            ),
            JwtIssuer(
                acceptedAudienceList = listOf(environment.azureAppClientId),
                jwtIssuerType = JwtIssuerType.INTERN_AZUREAD_V2,
                wellKnown = wellKnownInternADV2,
            ),
        ),
    )

    val azureAdV2Client = AzureAdV2Client(
        azureAppClientId = environment.azureAppClientId,
        azureAppClientSecret = environment.azureAppClientSecret,
        azureTokenEndpoint = environment.azureTokenEndpoint,
    )
    val veilederTilgangskontrollClient = Tilgangskontroll(
        azureAdV2Client = azureAdV2Client,
        baseUrl = environment.tilgangskontrollUrl,
        syfotilgangskontrollClientId = environment.syfotilgangskontrollClientId,
    )

    routing {
        registerPodApi(applicationState)
        authenticate(JwtIssuerType.INTERN_AZUREAD_V1.name) {
            registerPrediksjon(
                database,
                veilederTilgangskontrollClient,
                MidlertidigTilgangsSjekk(environment.tilgangPath),
            )
        }
        authenticate(JwtIssuerType.INTERN_AZUREAD_V2.name) {
            registerPrediksjonApiV2(
                database,
                veilederTilgangskontrollClient,
                MidlertidigTilgangsSjekk(environment.tilgangPath),
            )
        }
    }
}
