package no.nav.syfo.application.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.http.*
import io.ktor.http.HttpHeaders.Authorization
import io.ktor.server.testing.*
import no.nav.syfo.metric.COUNT_PREDIKSJON_OUTPUT_ERROR
import no.nav.syfo.metric.COUNT_PREDIKSJON_OUTPUT_FAILED
import no.nav.syfo.metric.COUNT_PREDIKSJON_OUTPUT_FORBIDDEN
import no.nav.syfo.metric.COUNT_PREDIKSJON_OUTPUT_SUCCESS
import no.nav.syfo.prediksjon.PrediksjonFrontend
import no.nav.syfo.prediksjon.createPrediksjonOutputTest
import no.nav.syfo.prediksjon.toPrediksjonFrontend
import no.nav.syfo.util.bearerHeader
import no.nav.syfo.util.configuredJacksonMapper
import org.amshove.kluent.shouldBeEqualTo
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import testutil.*
import testutil.UserConstants.ARBEIDSTAKER_FNR
import testutil.UserConstants.ARBEIDSTAKER_VEILEDER_NO_ACCESS
import testutil.UserConstants.VEILEDER_IDENT
import testutil.generator.generateOldPrediksjonOutput
import testutil.generator.generatePrediksjonOutputLong

class PrediksjonApiV2Spek : Spek({
    val objectMapper: ObjectMapper = configuredJacksonMapper()

    describe(PrediksjonApiV2Spek::class.java.simpleName) {

        with(TestApplicationEngine()) {
            start()

            val externalMockEnvironment = ExternalMockEnvironment()
            val database = externalMockEnvironment.database

            application.testApiModule(
                externalMockEnvironment = externalMockEnvironment,
            )

            beforeGroup {
                externalMockEnvironment.startExternalMocks()
            }

            afterGroup {
                externalMockEnvironment.stopExternalMocks()
            }

            afterEachTest {
                database.connection.dropData("prediksjon_output")
            }

            val url = "$apiV2BasePath$apiV2PrediksjonPath"
            val validToken = generateJWT(
                audience = externalMockEnvironment.environment.azureAppClientId,
                issuer = externalMockEnvironment.wellKnownInternADV2.issuer,
                navIdent = VEILEDER_IDENT,
            )

            describe("Successful get") {
                it("should return the latest prediksjon if request is successful") {
                    val oldPrediksjon = generateOldPrediksjonOutput
                    database.createPrediksjonOutputTest(
                        oldPrediksjon,
                        1
                    )
                    val wantedPrediksjon = generatePrediksjonOutputLong
                    database.createPrediksjonOutputTest(
                        wantedPrediksjon,
                        2
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

                        val actualPrediksjon = objectMapper.readValue<PrediksjonFrontend>(response.content!!)
                        val wantedPrediksjonFrontend = wantedPrediksjon.toPrediksjonFrontend()

                        actualPrediksjon.langt shouldBeEqualTo wantedPrediksjonFrontend.langt
                        actualPrediksjon.kortereVarighetGrunner shouldBeEqualTo wantedPrediksjonFrontend.kortereVarighetGrunner
                        actualPrediksjon.lengreVarighetGrunner shouldBeEqualTo wantedPrediksjonFrontend.lengreVarighetGrunner
                        actualPrediksjon.prediksjonsDato.toLocalDate() shouldBeEqualTo wantedPrediksjonFrontend.prediksjonsDato.toLocalDate()
                    }
                }

                it("should return 204 No Content if request is successful, but no prediksjoner exists on given person") {
                    val counter = COUNT_PREDIKSJON_OUTPUT_SUCCESS.get()
                    with(
                        handleRequest(HttpMethod.Get, url) {
                            addHeader(Authorization, bearerHeader(validToken))
                            addHeader(NAV_PERSONIDENT_HEADER, ARBEIDSTAKER_FNR.value)
                        }
                    ) {
                        response.status() shouldBeEqualTo HttpStatusCode.NoContent

                        COUNT_PREDIKSJON_OUTPUT_SUCCESS.get() shouldBeEqualTo counter.inc()
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
                    database.pause()
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
                        database.resume()
                    }
                }
            }
        }
    }
})
