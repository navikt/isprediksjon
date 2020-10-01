package testutil.mock

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.jackson.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import testutil.UserConstants

fun mockSyketilfelleServer(
    port: Int,
    kOppfolgingstilfelleJson: String
): NettyApplicationEngine {
    return embeddedServer(
        factory = Netty,
        port = port
    ) {
        install(ContentNegotiation) {
            jackson {}
        }
        routing {
            get("/kafka/oppfolgingstilfelle/beregn/${UserConstants.ARBEIDSTAKER_AKTORID.value}") {
                call.respond(kOppfolgingstilfelleJson)
            }
        }
    }
}
