package testutil.mock

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import no.nav.syfo.application.api.installContentNegotiation
import no.nav.syfo.clients.tilgangskontroll.Tilgangskontroll.Companion.TILGANGSKONTROLL_V2_PERSON_PATH
import no.nav.syfo.clients.tilgangskontroll.Tilgangskontroll.Tilgang
import testutil.UserConstants.ARBEIDSTAKER_FNR
import testutil.UserConstants.ARBEIDSTAKER_VEILEDER_NO_ACCESS
import testutil.getRandomPort

class VeilederTilgangskontrollMock {
    private val port = getRandomPort()
    val url = "http://localhost:$port"
    val tilgangFalse = Tilgang(
        false,
        ""
    )
    val tilgangTrue = Tilgang(
        true,
        ""
    )

    val name = "veiledertilgangskontroll"
    val server = mockTilgangServer(
        port,
        tilgangFalse,
        tilgangTrue
    )

    private fun mockTilgangServer(
        port: Int,
        tilgangFalse: Tilgang,
        tilgangTrue: Tilgang,
    ): NettyApplicationEngine {
        return embeddedServer(
            factory = Netty,
            port = port
        ) {
            installContentNegotiation()
            routing {
                get("/syfo-tilgangskontroll/api/tilgang/bruker") {
                    if (ARBEIDSTAKER_VEILEDER_NO_ACCESS.value == call.parameters["fnr"]) {
                        call.respond(HttpStatusCode.Forbidden, tilgangFalse)
                    } else {
                        call.respond(tilgangTrue)
                    }
                }
                get("$TILGANGSKONTROLL_V2_PERSON_PATH/${ARBEIDSTAKER_FNR.value}") {
                    call.respond(tilgangTrue)
                }
                get(TILGANGSKONTROLL_V2_PERSON_PATH) {
                    call.respond(HttpStatusCode.Forbidden, tilgangFalse)
                }
            }
        }
    }
}
