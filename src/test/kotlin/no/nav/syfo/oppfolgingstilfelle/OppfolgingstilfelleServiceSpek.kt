package no.nav.syfo.oppfolgingstilfelle

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.jackson.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.testing.*
import io.ktor.util.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import no.nav.syfo.clients.aktor.AktorService
import no.nav.syfo.clients.aktor.AktorregisterClient
import no.nav.syfo.clients.aktor.IdentType
import no.nav.syfo.clients.sts.StsRestClient
import no.nav.syfo.clients.syketilfelle.SyketilfelleClient
import no.nav.syfo.prediksjon.PrediksjonInputService
import no.nav.syfo.util.NAV_PERSONIDENTER
import org.amshove.kluent.shouldBeEqualTo
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import testutil.*
import testutil.UserConstants.ARBEIDSTAKER_AKTORID
import testutil.UserConstants.ARBEIDSTAKER_FNR
import testutil.generator.generateKOppfolgingstilfelle
import testutil.generator.generateKOppfolgingstilfellePeker
import java.net.ServerSocket

data class RSIdent(
    val ident: String,
    val identgruppe: String,
    val gjeldende: Boolean
)

data class RSAktor(
    val identer: List<RSIdent>? = null,
    val feilmelding: String? = null
)

@InternalAPI
object OppfolgingstilfelleServiceSpek : Spek({

    val objectMapper: ObjectMapper = ObjectMapper().apply {
        registerKotlinModule()
        registerModule(JavaTimeModule())
        configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true)
    }

    with(TestApplicationEngine()) {
        start()

        val env = testEnvironment(
            getRandomPort(),
            ""
        )

        val database = TestDB()

        val kOppfolgingstilfellePeker = generateKOppfolgingstilfellePeker
        val kOppfolgingstilfelle = generateKOppfolgingstilfelle

        val kOppfolgingstilfelleJson = objectMapper.writeValueAsString(kOppfolgingstilfelle)

        val mockHttpServerPort = ServerSocket(0).use { it.localPort }
        val mockHttpServerUrl = "http://localhost:$mockHttpServerPort"
        val mockServer = embeddedServer(Netty, mockHttpServerPort) {
            install(ContentNegotiation) {
                jackson {}
            }
            routing {
                get("/${env.syketilfelleUrl}/kafka/oppfolgingstilfelle/beregn/${ARBEIDSTAKER_AKTORID.value}") {
                    call.respond(kOppfolgingstilfelleJson)
                }
                get("/${env.aktorregisterV1Url}/identer") {
                    when (call.request.headers[NAV_PERSONIDENTER]) {
                        ARBEIDSTAKER_AKTORID.value -> {
                            call.respond(
                                mapOf(
                                    ARBEIDSTAKER_AKTORID.value to RSAktor(
                                        listOf(
                                            RSIdent(
                                                ident = ARBEIDSTAKER_AKTORID.value,
                                                identgruppe = IdentType.AktoerId.name,
                                                gjeldende = true
                                            ),
                                            RSIdent(
                                                ident = ARBEIDSTAKER_FNR.value,
                                                identgruppe = IdentType.NorskIdent.name,
                                                gjeldende = true
                                            )
                                        ),
                                        feilmelding = null
                                    )
                                )
                            )
                        }
                        else -> error("Something went wrong")
                    }
                }
            }
        }.start()

        val stsRestClient = mockk<StsRestClient>()
        val aktorregisterClient = AktorregisterClient("$mockHttpServerUrl/${env.aktorregisterV1Url}", stsRestClient)
        val aktorService = AktorService(aktorregisterClient)
        val prediksjonInputService = PrediksjonInputService(database)
        val syketilfelleClient = SyketilfelleClient("$mockHttpServerUrl/${env.syketilfelleUrl}", stsRestClient)

        val oppfolgingstilfelleService = OppfolgingstilfelleService(
            aktorService,
            prediksjonInputService,
            syketilfelleClient
        )

        afterGroup {
            database.stop()
            mockServer.stop(1L, 10L)
            unmockkAll()
        }

        beforeEachTest {
            every { stsRestClient.token() } returns "oidctoken"
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
