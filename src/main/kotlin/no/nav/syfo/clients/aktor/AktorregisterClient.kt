package no.nav.syfo.clients.aktor

import arrow.core.Either
import arrow.core.flatMap
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import no.nav.syfo.clients.sts.StsRestClient
import no.nav.syfo.util.*
import org.json.JSONObject
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger(AktorregisterClient::class.java)

class AktorregisterClient(
    private val baseUrl: String,
    private val stsRestClient: StsRestClient
) {
    private val client = HttpClient(CIO) {
        install(JsonFeature) {
            serializer = JacksonSerializer {
                registerKotlinModule()
                registerModule(JavaTimeModule())
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            }
        }
    }

    suspend fun getIdenter(ident: String, callId: String): Either<String, List<Ident>> {
        val bearer = stsRestClient.token()

        val url = "$baseUrl/identer?gjeldende=true"
        val response: HttpResponse = client.get(url) {
            header(HttpHeaders.Authorization, bearerHeader(bearer))
            header(NAV_CALL_ID, callId)
            header(NAV_CONSUMER_ID, APP_CONSUMER_ID)
            header(NAV_PERSONIDENTER, ident)
            accept(ContentType.Application.Json)
        }

        val responseJSONObject = JSONObject(response.receive<String>())

        val identResponse = responseJSONObject.getJSONObject(ident)

        return if (identResponse.isNull("identer")) {
            val errorMessage = identResponse.getString("feilmelding")
            log.error("lookup gjeldende identer feilet med feilmelding $errorMessage")
            Either.Left(errorMessage)
        } else {
            val identer = identResponse.getJSONArray("identer")

            Either.Right(
                identer.map {
                    it as JSONObject
                }.map {
                    Ident(
                        it.getString("ident"),
                        it.getEnum(
                            IdentType::class.java,
                            "identgruppe"
                        )
                    )
                }
            )
        }
    }

    private suspend fun getIdent(
        ident: String,
        type: IdentType,
        callId: String
    ): Either<String, String> {
        return getIdenter(ident, callId).flatMap { ident ->
            Either.Right(
                ident.first {
                    it.type == type
                }.ident
            )
        }
    }

    suspend fun getNorskIdent(ident: String, callId: String): Either<String, String> {
        return getIdent(ident, IdentType.NorskIdent, callId)
    }
}

enum class IdentType {
    AktoerId, NorskIdent
}

data class Ident(
    val ident: String,
    val type: IdentType
)
