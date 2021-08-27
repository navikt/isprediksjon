package testutil

import no.nav.syfo.application.Environment
import no.nav.syfo.application.VaultSecrets
import no.nav.syfo.application.ApplicationState
import java.net.ServerSocket
import java.util.*

fun testEnvironment(
    port: Int,
    kafkaBootstrapServers: String,
    azureTokenEndpoint: String = "azureTokenEndpoint",
    syfotilgangskontrollUrl: String = ""
) = Environment(
    applicationName = "isprediksjon",
    applicationPort = port,
    kafkaBootstrapServers = kafkaBootstrapServers,
    sm2013ManuellBehandlingTopic = "topic1",
    sm2013AutomatiskBehandlingTopic = "topic2",
    smregisterRecievedSykmeldingBackupTopic = "topic3",
    sm2013BehandlingsutfallTopic = "topic4",
    smregisterBehandlingsutfallBackupTopic = "topic5",
    syfoSykmeldingstatusLeesahTopic = "topic6",
    syfoRegisterStatusBackupTopic = "topic7",
    databaseMountPathVault = "vault.adeo.no",
    databaseName = "isprediksjon",
    isprediksjonDBURL = "12314.adeo.no",
    aktorregisterV1Url = "http://aktorregister",
    stsRestUrl = "http://stsrest",
    azureAppClientId = "app-client-id",
    azureAppClientSecret = "app-secret",
    azureAppWellKnownUrl = "wellknownurl",
    azureTokenEndpoint = azureTokenEndpoint,
    syketilfelleUrl = "http://syfosyketilfelle:0001",
    developmentMode = true,
    syfotilgangskontrollClientId = "syfotilgangskontrollClientId",
    tilgangskontrollUrl = syfotilgangskontrollUrl,
    tilgangPath = "./src/test/resources/tilgang.json"
)

val testVaultSecrets = VaultSecrets(
    "username",
    "password"
)

fun testAppState() = ApplicationState(
    alive = true,
    ready = true,
)

fun Properties.overrideForTest(): Properties = apply {
    remove("security.protocol")
    remove("sasl.mechanism")
}

fun getRandomPort() = ServerSocket(0).use {
    it.localPort
}
