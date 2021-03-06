package testutil.mock

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.jackson.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
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
            install(ContentNegotiation) {
                jackson {
                    registerKotlinModule()
                    registerModule(JavaTimeModule())
                    configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                }
            }
            routing {
                get("/kafka/oppfolgingstilfelle/beregn/${UserConstants.ARBEIDSTAKER_AKTORID.value}") {
                    call.respond(kOppfolgingstilfellePerson)
                }
            }
        }
    }
}
