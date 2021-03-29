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
import no.nav.syfo.prediksjon.PrediksjonFrontend
import no.nav.syfo.prediksjon.createPrediksjonOutputTest
import no.nav.syfo.prediksjon.toPrediksjonFrontend
import no.nav.syfo.serverModule
import no.nav.syfo.util.bearerHeader
import org.amshove.kluent.shouldBeEqualTo
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import testutil.*
import testutil.UserConstants.ARBEIDSTAKER_FNR
import testutil.UserConstants.ARBEIDSTAKER_VEILEDER_NO_ACCESS
import testutil.UserConstants.VEILEDER_IDENT
import testutil.generator.generateOldPrediksjonOutput
import testutil.generator.generatePrediksjonOutputLong
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

            val database = TestDB()

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

                    with(
                        handleRequest(HttpMethod.Get, url) {
                            addHeader(Authorization, bearerHeader(validToken))
                            addHeader(NAV_PERSONIDENT_HEADER, ARBEIDSTAKER_FNR.value)
                        }
                    ) {
                        response.status() shouldBeEqualTo HttpStatusCode.OK

                        val actualPrediksjon = objectMapper.readValue<PrediksjonFrontend>(response.content!!)

                        val wantedPrediksjonFrontend = wantedPrediksjon.toPrediksjonFrontend()

                        actualPrediksjon.langt shouldBeEqualTo wantedPrediksjonFrontend.langt
                        actualPrediksjon.kortereVarighetGrunner shouldBeEqualTo wantedPrediksjonFrontend.kortereVarighetGrunner
                        actualPrediksjon.lengreVarighetGrunner shouldBeEqualTo wantedPrediksjonFrontend.lengreVarighetGrunner
                        actualPrediksjon.prediksjonsDato.toLocalDate() shouldBeEqualTo wantedPrediksjonFrontend.prediksjonsDato.toLocalDate()
                    }
                }

                it("should return 204 No Content if request is successful, but no prediksjoner exists on given person") {
                    with(
                        handleRequest(HttpMethod.Get, url) {
                            addHeader(Authorization, bearerHeader(validToken))
                            addHeader(NAV_PERSONIDENT_HEADER, ARBEIDSTAKER_FNR.value)
                        }
                    ) {
                        response.status() shouldBeEqualTo HttpStatusCode.NoContent
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
                    with(
                        handleRequest(HttpMethod.Get, url) {
                            addHeader(Authorization, bearerHeader(validToken))
                        }
                    ) {
                        response.status() shouldBeEqualTo HttpStatusCode.BadRequest
                    }
                }

                it("Should return 403 Forbidden if veileder doesn't have access to user") {
                    with(
                        handleRequest(HttpMethod.Get, url) {
                            addHeader(Authorization, bearerHeader(validToken))
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
