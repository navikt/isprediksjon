package testutil.mock

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import no.nav.syfo.application.api.installContentNegotiation
import no.nav.syfo.clients.syketilfelle.domain.KOppfolgingstilfellePerson
import testutil.UserConstants
import testutil.generator.generateKOppfolgingstilfelle
import testutil.getRandomPort

class SyketilfelleMock {
    private val port = getRandomPort()
    val url = "http://localhost:$port"
    val kOppfolgingstilfellePerson = generateKOppfolgingstilfelle
    val server = mockSyketilfelleServer(port, kOppfolgingstilfellePerson)

    private fun mockSyketilfelleServer(
        port: Int,
        kOppfolgingstilfellePerson: KOppfolgingstilfellePerson
    ): NettyApplicationEngine {
        return embeddedServer(
            factory = Netty,
            port = port
        ) {
            installContentNegotiation()
            routing {
                get("/kafka/oppfolgingstilfelle/beregn/${UserConstants.ARBEIDSTAKER_AKTORID.value}") {
                    call.respond(kOppfolgingstilfellePerson)
                }
            }
        }
    }
}
