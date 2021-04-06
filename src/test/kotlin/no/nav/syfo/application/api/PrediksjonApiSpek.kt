package no.nav.syfo.application.api

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.http.*
import io.ktor.http.HttpHeaders.Authorization
import io.ktor.server.testing.*
import no.nav.syfo.application.ApplicationState
import no.nav.syfo.metric.COUNT_PREDIKSJON_OUTPUT_ERROR
import no.nav.syfo.metric.COUNT_PREDIKSJON_OUTPUT_FAILED
import no.nav.syfo.metric.COUNT_PREDIKSJON_OUTPUT_FORBIDDEN
import no.nav.syfo.metric.COUNT_PREDIKSJON_OUTPUT_SUCCESS
import no.nav.syfo.prediksjon.PrediksjonOutput
import no.nav.syfo.prediksjon.createPrediksjonOutputTest
import no.nav.syfo.serverModule
import no.nav.syfo.util.bearerHeader
import org.amshove.kluent.shouldBeEqualTo
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import testutil.*
import testutil.UserConstants.ARBEIDSTAKER_FNR
import testutil.UserConstants.ARBEIDSTAKER_VEILEDER_NO_ACCESS
import testutil.UserConstants.VEILEDER_IDENT
import testutil.generator.generatePrediksjonOutput
import testutil.mock.VeilederTilgangskontrollMock
import testutil.mock.wellKnownMock

class PrediksjonApiSpek : Spek({
    val objectMapper: ObjectMapper = ObjectMapper().apply {
        registerKotlinModule()
        registerModule(JavaTimeModule())
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
    }

    describe("PrediksjonApiSpek") {

        with(TestApplicationEngine()) {
            start()

            val tilgangskontrollMock = VeilederTilgangskontrollMock()

            val applicationState = ApplicationState(
                alive = true,
                ready = true
            )

            var database = TestDB()

            val environment = testEnvironment(
                getRandomPort(),
                "",
                tilgangskontrollUrl = tilgangskontrollMock.url
            )

            val wellKnown = wellKnownMock()

            application.serverModule(
                applicationState = applicationState,
                database = database,
                env = environment,
                wellKnown = wellKnown
            )

            beforeGroup {
                tilgangskontrollMock.server.start()
            }

            afterEachTest {
                database.connection.dropData("prediksjon_output")
            }

            afterGroup {
                tilgangskontrollMock.server.stop(1L, 10L)

                database.stop()
            }

            val url = "$apiBasePath$apiPrediksjon"
            val validToken = generateJWT(
                environment.loginserviceClientId,
                wellKnown.issuer,
                VEILEDER_IDENT,
            )

            describe("Successful get") {
                it("should return DialogmoteList if request is successful") {
                    database.createPrediksjonOutputTest(
                        generatePrediksjonOutput,
                        1
                    )

                    val counter = COUNT_PREDIKSJON_OUTPUT_SUCCESS.get()
                    with(
                        handleRequest(HttpMethod.Get, url) {
                            addHeader(Authorization, bearerHeader(validToken))
                            addHeader(NAV_PERSONIDENT_HEADER, ARBEIDSTAKER_FNR.value)
                        }
                    ) {
                        response.status() shouldBeEqualTo HttpStatusCode.OK

                        COUNT_PREDIKSJON_OUTPUT_SUCCESS.get() shouldBeEqualTo counter.inc()

                        val prediksjonList = objectMapper.readValue<List<PrediksjonOutput>>(response.content!!)

                        prediksjonList.size shouldBeEqualTo 1

                        val prediksjon = prediksjonList[0]
                        prediksjon.fnr shouldBeEqualTo ARBEIDSTAKER_FNR
                    }
                }

                it("should return empty list if request is successful, but no prediksjoner exists on given person") {
                    val counter = COUNT_PREDIKSJON_OUTPUT_SUCCESS.get()
                    with(
                        handleRequest(HttpMethod.Get, url) {
                            addHeader(Authorization, bearerHeader(validToken))
                            addHeader(NAV_PERSONIDENT_HEADER, ARBEIDSTAKER_FNR.value)
                        }
                    ) {
                        response.status() shouldBeEqualTo HttpStatusCode.OK

                        COUNT_PREDIKSJON_OUTPUT_SUCCESS.get() shouldBeEqualTo counter.inc()
                        val prediksjonList = objectMapper.readValue<List<PrediksjonOutput>>(response.content!!)

                        prediksjonList.size shouldBeEqualTo 0
                    }
                }
            }

            describe("Failing get") {
                it("Should return 401 Unauthorized if no token is given as header") {
                    with(
                        handleRequest(HttpMethod.Get, url) {
                            addHeader(NAV_PERSONIDENT_HEADER, ARBEIDSTAKER_FNR.value)
                        }
                    ) {
                        response.status() shouldBeEqualTo HttpStatusCode.Unauthorized
                    }
                }

                it("Should return BadRequest if no fnr is given as header") {
                    val counter = COUNT_PREDIKSJON_OUTPUT_FAILED.get()
                    with(
                        handleRequest(HttpMethod.Get, url) {
                            addHeader(Authorization, bearerHeader(validToken))
                        }
                    ) {
                        response.status() shouldBeEqualTo HttpStatusCode.BadRequest
                        COUNT_PREDIKSJON_OUTPUT_FAILED.get() shouldBeEqualTo counter.inc()
                    }
                }

                it("Should return 403 Forbidden if veileder doesn't have access to user") {
                    val counter = COUNT_PREDIKSJON_OUTPUT_FORBIDDEN.get()
                    with(
                        handleRequest(HttpMethod.Get, url) {
                            addHeader(Authorization, bearerHeader(validToken))
                            addHeader(NAV_PERSONIDENT_HEADER, ARBEIDSTAKER_VEILEDER_NO_ACCESS.value)
                        }
                    ) {
                        response.status() shouldBeEqualTo HttpStatusCode.Forbidden
                        COUNT_PREDIKSJON_OUTPUT_FORBIDDEN.get() shouldBeEqualTo counter.inc()
                    }
                }
            }
            describe("Database down") {
                it("should return error when database fails") {
                    database.stop()
                    try {
                        val counterSuccess = COUNT_PREDIKSJON_OUTPUT_SUCCESS.get()
                        val counterError = COUNT_PREDIKSJON_OUTPUT_ERROR.get()
                        with(
                            handleRequest(HttpMethod.Get, url) {
                                addHeader(Authorization, bearerHeader(validToken))
                                addHeader(NAV_PERSONIDENT_HEADER, ARBEIDSTAKER_FNR.value)
                            }
                        ) {
                            response.status() shouldBeEqualTo HttpStatusCode.InternalServerError

                            COUNT_PREDIKSJON_OUTPUT_SUCCESS.get() shouldBeEqualTo counterSuccess
                            COUNT_PREDIKSJON_OUTPUT_ERROR.get() shouldBeEqualTo counterError.inc()
                        }
                    } finally {
                        // Recreate database
                        database = TestDB()
                    }
                }
            }
        }
    }
})
