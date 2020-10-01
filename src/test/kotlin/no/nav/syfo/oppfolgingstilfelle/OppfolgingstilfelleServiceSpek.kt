package no.nav.syfo.oppfolgingstilfelle

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.server.testing.*
import io.ktor.util.*
import no.nav.syfo.clients.aktor.AktorService
import no.nav.syfo.clients.aktor.AktorregisterClient
import no.nav.syfo.clients.sts.StsRestClient
import no.nav.syfo.clients.syketilfelle.SyketilfelleClient
import no.nav.syfo.prediksjon.PrediksjonInputService
import org.amshove.kluent.shouldBeEqualTo
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import testutil.*
import testutil.UserConstants.ARBEIDSTAKER_FNR
import testutil.generator.generateKOppfolgingstilfelle
import testutil.generator.generateKOppfolgingstilfellePeker
import testutil.mock.mockAktorregisterServer
import testutil.mock.mockStsRestServer
import testutil.mock.mockSyketilfelleServer

@InternalAPI
object OppfolgingstilfelleServiceSpek : Spek({

    val objectMapper: ObjectMapper = ObjectMapper().apply {
        registerKotlinModule()
        registerModule(JavaTimeModule())
        configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true)
    }

    with(TestApplicationEngine()) {
        start()

        val vaultSecrets = vaultSecrets

        val database = TestDB()

        val kOppfolgingstilfellePeker = generateKOppfolgingstilfellePeker
        val kOppfolgingstilfelle = generateKOppfolgingstilfelle

        val kOppfolgingstilfelleJson = objectMapper.writeValueAsString(kOppfolgingstilfelle)

        val syketilfelleServerPort = getRandomPort()
        val syketilfelleServerUrl = "http://localhost:$syketilfelleServerPort"
        val syketilfelleServer = mockSyketilfelleServer(
            syketilfelleServerPort,
            kOppfolgingstilfelleJson
        ).start()

        val stsRestServerPort = getRandomPort()
        val stsRestServerUrl = "http://localhost:$stsRestServerPort"
        val stsRestServer = mockStsRestServer(
            stsRestServerPort
        ).start()

        val aktorregisterServerPort = getRandomPort()
        val aktorregisterServerUrl = "http://localhost:$aktorregisterServerPort"
        val aktorregisterServer = mockAktorregisterServer(
            aktorregisterServerPort
        ).start()

        val stsRestClient = StsRestClient(
            baseUrl = stsRestServerUrl,
            serviceuserUsername = vaultSecrets.serviceuserUsername,
            serviceuserPassword = vaultSecrets.serviceuserPassword
        )
        val aktorregisterClient = AktorregisterClient(aktorregisterServerUrl, stsRestClient)
        val aktorService = AktorService(aktorregisterClient)
        val prediksjonInputService = PrediksjonInputService(database)
        val syketilfelleClient = SyketilfelleClient(syketilfelleServerUrl, stsRestClient)

        val oppfolgingstilfelleService = OppfolgingstilfelleService(
            aktorService,
            prediksjonInputService,
            syketilfelleClient
        )

        afterGroup {
            database.stop()
            aktorregisterServer.stop(1L, 10L)
            stsRestServer.stop(1L, 10L)
            syketilfelleServer.stop(1L, 10L)
        }

        afterEachTest {
            val table = "prediksjon_input"
            database.connection.dropData(table)
        }

        describe("Read and store PPrediksjonInput") {

            it("should store PrediksjonInput based on Oppfolgingstilfelle") {
                oppfolgingstilfelleService.receiveOppfolgingstilfelle(kOppfolgingstilfellePeker)

                val prediksjonInputFnrList =
                    database.connection.getPrediksjonInput1()

                prediksjonInputFnrList.size shouldBeEqualTo 1

                val returnedPrediksjonInput = prediksjonInputFnrList.first()

                returnedPrediksjonInput.fnr shouldBeEqualTo ARBEIDSTAKER_FNR.value
                returnedPrediksjonInput.aktorId shouldBeEqualTo kOppfolgingstilfellePeker.aktorId
                returnedPrediksjonInput.tilfelleStartDate shouldBeEqualTo kOppfolgingstilfelle.tidslinje.first().dag
                returnedPrediksjonInput.tilfelleEndDate shouldBeEqualTo kOppfolgingstilfelle.tidslinje.last().dag
            }
        }
    }
})
