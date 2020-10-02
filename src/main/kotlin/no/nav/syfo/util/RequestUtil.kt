package no.nav.syfo.util

import java.util.*

const val APP_CONSUMER_ID = "isprediksjon"
const val NAV_CONSUMER_ID = "Nav-Consumer-Id"

const val NAV_PERSONIDENTER = "Nav-Personidenter"
const val NAV_CALL_ID = "Nav-Call-Id"

fun basicHeader(
    credentialUsername: String,
    credentialPassword: String
) = "Basic " + Base64.getEncoder().encodeToString(java.lang.String.format("%s:%s", credentialUsername, credentialPassword).toByteArray())

fun bearerHeader(token: String) = "Bearer $token"
