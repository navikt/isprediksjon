package testutil.mock

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import no.nav.syfo.application.api.installContentNegotiation
import no.nav.syfo.clients.tilgangskontroll.Tilgangskontroll.Companion.TILGANGSKONTROLL_PERSON_PATH
import no.nav.syfo.clients.tilgangskontroll.Tilgangskontroll.Tilgang
import no.nav.syfo.util.NAV_PERSONIDENT_HEADER
import testutil.UserConstants.ARBEIDSTAKER_FNR
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
                get(TILGANGSKONTROLL_PERSON_PATH) {
                    if (call.request.headers[NAV_PERSONIDENT_HEADER] == ARBEIDSTAKER_FNR.value) {
                        call.respond(tilgangTrue)
                    } else {
                        call.respond(HttpStatusCode.Forbidden, tilgangFalse)
                    }
                }
            }
        }
    }
}
