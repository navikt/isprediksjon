package no.nav.syfo.auth

import com.auth0.jwk.JwkProviderBuilder
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import net.logstash.logback.argument.StructuredArguments
import java.net.URL
import java.util.concurrent.TimeUnit
import org.apache.http.impl.conn.SystemDefaultRoutePlanner
import java.net.ProxySelector

fun Application.auth(wellKnown: WellKnown, acceptedAudienceList: List<String>) {
    log.info("Initialization of auth starting")

    val jwkProvider = JwkProviderBuilder(
        URL(wellKnown.jwks_uri)
    )
        .cached(10, 24, TimeUnit.HOURS)
        .rateLimited(10, 1, TimeUnit.MINUTES)
        .build()

    install(Authentication) {
        jwt {
            verifier(jwkProvider, wellKnown.issuer)
            validate { credentials ->
                if (hasExpectedAudience(credentials, acceptedAudienceList)) {
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
    log.info("Initialization of auth done")
}

fun hasExpectedAudience(credentials: JWTCredential, expectedAudience: List<String>): Boolean {
    return expectedAudience.any { credentials.payload.audience.contains(it) }
}

data class WellKnown(
    val authorization_endpoint: String,
    val token_endpoint: String,
    val jwks_uri: String,
    val issuer: String
)

val proxyConfig: HttpClientConfig<ApacheEngineConfig>.() -> Unit = {
    install(JsonFeature) {
        serializer = JacksonSerializer {
            registerKotlinModule()
            registerModule(JavaTimeModule())
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }
    }
    engine {
        customizeClient {
            setRoutePlanner(SystemDefaultRoutePlanner(ProxySelector.getDefault()))
        }
    }
}

fun getWellKnown(wellKnownUrl: String) =
    runBlocking { HttpClient(Apache, proxyConfig).use { cli -> cli.get<WellKnown>(wellKnownUrl) } }
