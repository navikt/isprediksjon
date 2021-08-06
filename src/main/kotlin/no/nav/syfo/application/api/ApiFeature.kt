package no.nav.syfo.application.api

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.jackson.*
import no.nav.syfo.util.configureJacksonMapper

fun Application.installContentNegotiation() {
    install(ContentNegotiation) {
        jackson(block = configureJacksonMapper())
    }
}
