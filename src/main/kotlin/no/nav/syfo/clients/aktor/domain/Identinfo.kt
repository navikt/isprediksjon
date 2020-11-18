package no.nav.syfo.clients.aktor.domain

const val NO_IDENT_ERROR_MSG = "Den angitte personidenten finnes ikke"

data class Identinfo(
    val ident: String,
    val identgruppe: String,
    val gjeldende: Boolean = false
)
