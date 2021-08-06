package no.nav.syfo.clients.azuread.v2.wellknown

data class WellKnown(
    val authorization_endpoint: String,
    val token_endpoint: String,
    val jwks_uri: String,
    val issuer: String,
)
