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
import no.nav.syfo.prediksjon.PrediksjonOutput
import no.nav.syfo.prediksjon.createPrediksjonOutputTest
import no.nav.syfo.serverModule
import no.nav.syfo.util.bearerHeader
import org.amshove.kluent.shouldBeEqualTo
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import testutil.TestDB
import testutil.UserConstants.ARBEIDSTAKER_FNR
import testutil.UserConstants.ARBEIDSTAKER_VEILEDER_NO_ACCESS
import testutil.dropData
import testutil.generator.generatePrediksjonOutput
import testutil.getRandomPort
import testutil.mock.VeilederTilgangskontrollMock
import testutil.testEnvironment

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

            val database = TestDB()

            val environment = testEnvironment(
                getRandomPort(),
                "",
                tilgangskontrollUrl = tilgangskontrollMock.url
            )

            application.serverModule(
                applicationState = applicationState,
                database = database,
                env = environment,
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
            describe("Successful get") {
                it("should return DialogmoteList if request is successful") {
                    database.createPrediksjonOutputTest(
                        generatePrediksjonOutput,
                        1
                    )

                    with(
                        handleRequest(HttpMethod.Get, url) {
                            addHeader(Authorization, bearerHeader("validToken"))
                            addHeader(NAV_PERSONIDENT_HEADER, ARBEIDSTAKER_FNR.value)
                        }
                    ) {
                        response.status() shouldBeEqualTo HttpStatusCode.OK

                        val prediksjonList = objectMapper.readValue<List<PrediksjonOutput>>(response.content!!)

                        prediksjonList.size shouldBeEqualTo 1

                        val prediksjon = prediksjonList[0]
                        prediksjon.fnr shouldBeEqualTo ARBEIDSTAKER_FNR
                    }
                }

                it("should return empty list if request is successful, but no prediksjoner exists on given person") {
                    with(
                        handleRequest(HttpMethod.Get, url) {
                            addHeader(Authorization, bearerHeader("validToken"))
                            addHeader(NAV_PERSONIDENT_HEADER, ARBEIDSTAKER_FNR.value)
                        }
                    ) {
                        response.status() shouldBeEqualTo HttpStatusCode.OK

                        val prediksjonList = objectMapper.readValue<List<PrediksjonOutput>>(response.content!!)

                        prediksjonList.size shouldBeEqualTo 0
                    }
                }
            }

            describe("Failing get") {
                it("Should return BadRequest if no token is given as header") {
                    with(
                        handleRequest(HttpMethod.Get, url) {
                            addHeader(NAV_PERSONIDENT_HEADER, ARBEIDSTAKER_FNR.value)
                        }
                    ) {
                        response.status() shouldBeEqualTo HttpStatusCode.BadRequest
                    }
                }

                it("Should return BadRequest if no fnr is given as header") {
                    with(
                        handleRequest(HttpMethod.Get, url) {
                            addHeader(Authorization, bearerHeader("validToken"))
                        }
                    ) {
                        response.status() shouldBeEqualTo HttpStatusCode.BadRequest
                    }
                }

                it("Should return 403 Forbidden if veileder doesn't have access to user") {
                    with(
                        handleRequest(HttpMethod.Get, url) {
                            addHeader(Authorization, bearerHeader("validToken"))
                            addHeader(NAV_PERSONIDENT_HEADER, ARBEIDSTAKER_VEILEDER_NO_ACCESS.value)
                        }
                    ) {
                        response.status() shouldBeEqualTo HttpStatusCode.Forbidden
                    }
                }
            }
        }
    }
})
