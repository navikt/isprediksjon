package testutil

import no.nav.syfo.Environment
import no.nav.syfo.VaultSecrets
import java.net.ServerSocket
import java.util.*

fun testEnvironment(
    port: Int,
    kafkaBootstrapServers: String,
    azureTokenEndpoint: String = "azureTokenEndpoint",
    tilgangskontrollUrl: String = ""
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
    loginserviceClientId = "1234",
    aadDiscoveryUrl = "",
    azureAppClientId = "app-client-id",
    azureAppClientSecret = "app-secret",
    azureAppWellKnownUrl = "wellknownurl",
    azureTokenEndpoint = azureTokenEndpoint,
    syketilfelleUrl = "http://syfosyketilfelle:0001",
    developmentMode = true,
    syfotilgangskontrollClientId = "syfotilgangskontrollClientId",
    tilgangskontrollUrl = tilgangskontrollUrl,
    tilgangPath = "./src/test/resources/tilgang.json"
)

val vaultSecrets = VaultSecrets(
    "username",
    "password"
)

fun Properties.overrideForTest(): Properties = apply {
    remove("security.protocol")
    remove("sasl.mechanism")
}

fun getRandomPort() = ServerSocket(0).use {
    it.localPort
}
