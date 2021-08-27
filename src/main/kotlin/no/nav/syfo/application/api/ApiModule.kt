package no.nav.syfo.application.api

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.routing.*
import no.nav.syfo.application.Environment
import no.nav.syfo.application.ApplicationState
import no.nav.syfo.application.api.authentication.JwtIssuer
import no.nav.syfo.application.api.authentication.JwtIssuerType
import no.nav.syfo.clients.azuread.v2.wellknown.WellKnown
import no.nav.syfo.application.api.authentication.auth
import no.nav.syfo.tilgangskontroll.*
import no.nav.syfo.clients.tilgangskontroll.Tilgangskontroll
import no.nav.syfo.clients.azuread.v2.AzureAdV2Client
import no.nav.syfo.application.database.DatabaseInterface
import no.nav.syfo.prediksjon.api.registerPrediksjonApiV2

fun Application.apiModule(
    applicationState: ApplicationState,
    database: DatabaseInterface,
    environment: Environment,
    wellKnownInternADV2: WellKnown,
) {
    installContentNegotiation()
    auth(
        jwtIssuerList = listOf(
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
        registerPrometheusApi()
        authenticate(JwtIssuerType.INTERN_AZUREAD_V2.name) {
            registerPrediksjonApiV2(
                database,
                veilederTilgangskontrollClient,
                MidlertidigTilgangsSjekk(environment.tilgangPath),
            )
        }
    }
}
