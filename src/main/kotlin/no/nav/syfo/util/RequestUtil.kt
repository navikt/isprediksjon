package no.nav.syfo.util

import net.logstash.logback.argument.StructuredArguments
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

const val APP_CONSUMER_ID = "isprediksjon"
const val NAV_CONSUMER_ID = "Nav-Consumer-Id"

const val NAV_PERSONIDENTER = "Nav-Personidenter"
const val NAV_CALL_ID = "Nav-Call-Id"

fun basicHeader(
    credentialUsername: String,
    credentialPassword: String
) = "Basic " + Base64.getEncoder().encodeToString(java.lang.String.format("%s:%s", credentialUsername, credentialPassword).toByteArray())

fun bearerHeader(token: String) = "Bearer $token"

private val kafkaCounterOppfolgingstilfelle = AtomicInteger(0)

fun kafkaCallIdOppfolgingstilfelle(): String = "${LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-HHmm"))}-isprediksjon-kafka-oppfolgingstilfelle-${kafkaCounterOppfolgingstilfelle.incrementAndGet()}"

fun callIdArgument(callId: String) = StructuredArguments.keyValue("callId", callId)!!
