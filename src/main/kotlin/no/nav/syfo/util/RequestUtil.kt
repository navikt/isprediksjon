package no.nav.syfo.util

import io.ktor.application.*
import io.ktor.util.pipeline.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

const val APP_CONSUMER_ID = "isprediksjon"
const val NAV_CONSUMER_ID = "Nav-Consumer-Id"

const val NAV_PERSONIDENTER = "Nav-Personidenter"
const val NAV_CALL_ID = "Nav-Call-Id"

const val NAV_PERSONIDENT_HEADER = "nav-personident"

fun basicHeader(
    credentialUsername: String,
    credentialPassword: String
) = "Basic " + Base64.getEncoder().encodeToString(java.lang.String.format("%s:%s", credentialUsername, credentialPassword).toByteArray())

fun PipelineContext<out Unit, ApplicationCall>.getCallId(): String {
    return this.call.request.headers[NAV_CALL_ID].toString()
}

fun bearerHeader(token: String) = "Bearer $token"

private val kafkaCounterOppfolgingstilfelle = AtomicInteger(0)

fun kafkaCallIdOppfolgingstilfelle(): String = "${LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-HHmm"))}-isprediksjon-kafka-oppfolgingstilfelle-${kafkaCounterOppfolgingstilfelle.incrementAndGet()}"
