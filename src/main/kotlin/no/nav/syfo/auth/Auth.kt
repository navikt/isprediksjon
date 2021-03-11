package no.nav.syfo.auth

import com.auth0.jwk.JwkProviderBuilder
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import net.logstash.logback.argument.StructuredArguments
import no.nav.syfo.Environment
import java.util.concurrent.TimeUnit

fun Application.auth(env: Environment) {
    val jwkIssuer = env.aadDiscoveryUrl
    val jwkProvider = JwkProviderBuilder(jwkIssuer)
        .cached(10, 24, TimeUnit.HOURS)
        .rateLimited(10, 1, TimeUnit.MINUTES)
        .build()

    install(Authentication) {
        jwt {
            verifier(jwkProvider, jwkIssuer)
            validate { credentials ->
                if (hasExpectedAudience(credentials, listOf(env.loginserviceClientId))) {
                    JWTPrincipal(credentials.payload)
                } else {
                    log.warn(
                        "Auth: Unexpected audience for jwt {}, {}",
                        StructuredArguments.keyValue("issuer", credentials.payload.issuer),
                        StructuredArguments.keyValue("audience", credentials.payload.audience)
                    )
                    null
                }
            }
        }
    }
}

fun hasExpectedAudience(credentials: JWTCredential, expectedAudience: List<String>): Boolean {
    return expectedAudience.any { credentials.payload.audience.contains(it) }
}
