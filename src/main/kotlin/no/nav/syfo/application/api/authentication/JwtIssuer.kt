package no.nav.syfo.application.api.authentication

import no.nav.syfo.clients.azuread.v2.wellknown.WellKnown

data class JwtIssuer(
    val acceptedAudienceList: List<String>,
    val jwtIssuerType: JwtIssuerType,
    val wellKnown: WellKnown,
)

enum class JwtIssuerType {
    INTERN_AZUREAD_V1,
    INTERN_AZUREAD_V2,
}
