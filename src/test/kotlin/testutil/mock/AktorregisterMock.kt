package testutil.mock

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.jackson.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import no.nav.syfo.clients.aktor.IdentType
import no.nav.syfo.util.NAV_PERSONIDENTER
import testutil.UserConstants
import testutil.getRandomPort
import no.nav.syfo.clients.aktor.domain.NO_IDENT_ERROR_MSG

class AktorregisterMock {
    private val port = getRandomPort()
    val url = "http://localhost:$port"
    val server = mockAktorregisterServer(port)

    private fun mockAktorregisterServer(
        port: Int,
    ): NettyApplicationEngine {
        return embeddedServer(
            factory = Netty,
            port = port
        ) {
            install(ContentNegotiation) {
                jackson {}
            }
            routing {
                get("/identer") {
                    when (call.request.headers[NAV_PERSONIDENTER]) {
                        UserConstants.ARBEIDSTAKER_AKTORID.value -> {
                            call.respond(
                                mapOf(
                                    UserConstants.ARBEIDSTAKER_AKTORID.value to RSAktor(
                                        listOf(
                                            RSIdent(
                                                ident = UserConstants.ARBEIDSTAKER_AKTORID.value,
                                                identgruppe = IdentType.AktoerId.name,
                                                gjeldende = true
                                            ),
                                            RSIdent(
                                                ident = UserConstants.ARBEIDSTAKER_FNR.value,
                                                identgruppe = IdentType.NorskIdent.name,
                                                gjeldende = true
                                            )
                                        ),
                                        feilmelding = null
                                    )
                                )
                            )
                        }
                        UserConstants.ARBEIDSTAKER_AKTORID_FINNES_IKKE.value -> {
                            call.respond(
                                mapOf(
                                    UserConstants.ARBEIDSTAKER_AKTORID_FINNES_IKKE.value to RSAktor(
                                        null,
                                        feilmelding = NO_IDENT_ERROR_MSG
                                    )
                                )
                            )
                        }
                        else -> error("Something went wrong")
                    }
                }
            }
        }
    }
}

data class RSIdent(
    val ident: String,
    val identgruppe: String,
    val gjeldende: Boolean
)

data class RSAktor(
    val identer: List<RSIdent>? = null,
    val feilmelding: String? = null
)
