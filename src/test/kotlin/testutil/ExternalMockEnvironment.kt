package testutil

import io.ktor.server.netty.*
import no.nav.syfo.application.ApplicationState
import testutil.mock.AzureAdV2Mock
import testutil.mock.VeilederTilgangskontrollMock
import testutil.mock.wellKnownInternADV2Mock

class ExternalMockEnvironment {
    val applicationState: ApplicationState = testAppState()
    val database = TestDB()

    val azureAdV2Mock = AzureAdV2Mock()
    val tilgangskontrollMock = VeilederTilgangskontrollMock()

    val externalApplicationMockMap = hashMapOf(
        azureAdV2Mock.name to azureAdV2Mock.server,
        tilgangskontrollMock.name to tilgangskontrollMock.server,
    )

    val environment = testEnvironment(
        azureTokenEndpoint = azureAdV2Mock.url,
        kafkaBootstrapServers = "",
        port = getRandomPort(),
        syfotilgangskontrollUrl = tilgangskontrollMock.url,
    )
    val vaultSecrets = testVaultSecrets
    val wellKnownInternADV2 = wellKnownInternADV2Mock()
}

fun ExternalMockEnvironment.startExternalMocks() {
    this.externalApplicationMockMap.start()
}

fun ExternalMockEnvironment.stopExternalMocks() {
    this.externalApplicationMockMap.stop()
    this.database.stop()
}

fun HashMap<String, NettyApplicationEngine>.start() {
    this.forEach {
        it.value.start()
    }
}

fun HashMap<String, NettyApplicationEngine>.stop(
    gracePeriodMillis: Long = 1L,
    timeoutMillis: Long = 10L,
) {
    this.forEach {
        it.value.stop(gracePeriodMillis, timeoutMillis)
    }
}
