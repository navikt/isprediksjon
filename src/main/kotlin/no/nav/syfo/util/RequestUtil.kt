package no.nav.syfo.util

const val APP_CONSUMER_ID = "isprediksjon"
const val NAV_CONSUMER_ID = "Nav-Consumer-Id"

const val NAV_PERSONIDENTER = "Nav-Personidenter"
const val NAV_CALL_ID = "Nav-Call-Id"

fun bearerHeader(token: String) = "Bearer $token"
