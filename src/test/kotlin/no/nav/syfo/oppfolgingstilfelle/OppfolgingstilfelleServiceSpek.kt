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
import testutil.mock.mockSyketilfelleServer
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

data class OidcToken(
    val access_token: String,
    val expires_in: Long,
    val token_type: String
)

private val defaultToken = OidcToken(
    access_token = "default access token",
    expires_in = 3600,
    token_type = "Bearer"
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

        val vaultSecrets = vaultSecrets
        val env = testEnvironment(
            getRandomPort(),
            ""
        )

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

        val mockHttpServerPort = ServerSocket(0).use { it.localPort }
        val mockHttpServerUrl = "http://localhost:$mockHttpServerPort"
        val mockServer = embeddedServer(Netty, mockHttpServerPort) {
            install(ContentNegotiation) {
                jackson {}
            }
            routing {
                get("/rest/v1/sts/token") {
                    val params = call.request.queryParameters
                    if (params["grant_type"].equals("client_credentials") && params["scope"].equals("openid")) {
                        call.respond(defaultToken)
                    }
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

        val stsRestClient = StsRestClient(
            baseUrl = mockHttpServerUrl,
            serviceuserUsername = vaultSecrets.serviceuserUsername,
            serviceuserPassword = vaultSecrets.serviceuserPassword
        )
        val aktorregisterClient = AktorregisterClient("$mockHttpServerUrl/${env.aktorregisterV1Url}", stsRestClient)
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
            syketilfelleServer.stop(1L, 10L)
            mockServer.stop(1L, 10L)
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
