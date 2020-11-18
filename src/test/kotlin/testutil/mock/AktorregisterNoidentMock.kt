package testutil.mock

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.jackson.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import no.nav.syfo.util.NAV_PERSONIDENTER
import testutil.UserConstants
import testutil.getRandomPort

class AktorregisterNoidentMock {
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
                                        null,
                                        feilmelding = "Den angitte personidenten finnes ikke"
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
