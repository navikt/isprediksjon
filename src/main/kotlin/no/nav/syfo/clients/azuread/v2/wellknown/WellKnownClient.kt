package no.nav.syfo.clients.azuread.v2.wellknown

import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import no.nav.syfo.clients.proxyConfig

fun getWellKnown(wellKnownUrl: String) =
    runBlocking { HttpClient(Apache, proxyConfig).use { cli -> cli.get<WellKnown>(wellKnownUrl) } }
