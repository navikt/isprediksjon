package testutil

import io.ktor.application.*
import no.nav.syfo.application.api.apiModule

fun Application.testApiModule(
    externalMockEnvironment: ExternalMockEnvironment,
) {
    apiModule(
        applicationState = externalMockEnvironment.applicationState,
        database = externalMockEnvironment.database,
        environment = externalMockEnvironment.environment,
        wellKnownInternADV1 = externalMockEnvironment.wellKnownInternADV1,
        wellKnownInternADV2 = externalMockEnvironment.wellKnownInternADV2,
    )
}
