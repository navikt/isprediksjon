package testutil.mock

import com.fasterxml.jackson.databind.ObjectMapper
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

fun mockSyketilfelleServer(
    port: Int,
    kOppfolgingstilfellePerson: KOppfolgingstilfellePerson
): NettyApplicationEngine {
    val kOppfolgingstilfelleJson = objectMapper.writeValueAsString(kOppfolgingstilfellePerson)

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

private val objectMapper: ObjectMapper = ObjectMapper().apply {
    registerKotlinModule()
    registerModule(JavaTimeModule())
    configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true)
}
