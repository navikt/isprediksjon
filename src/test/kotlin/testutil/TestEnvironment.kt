package testutil

import no.nav.syfo.Environment
import no.nav.syfo.VaultSecrets
import java.net.ServerSocket
import java.util.*

fun testEnvironment(port: Int, kafkaBootstrapServers: String) = Environment(
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
    syketilfelleUrl = "http://syfosyketilfelle:0001"
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
