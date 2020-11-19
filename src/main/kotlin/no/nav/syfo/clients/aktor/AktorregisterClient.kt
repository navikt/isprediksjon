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
import no.nav.syfo.clients.aktor.domain.IdentinfoListe
import no.nav.syfo.clients.aktor.domain.NO_IDENT_ERROR_MSG
import no.nav.syfo.clients.sts.StsRestClient
import no.nav.syfo.util.*
import org.slf4j.LoggerFactory

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

        val mapResponse = response.receive<Map<String, IdentinfoListe>>()
        val identResponse: IdentinfoListe? = mapResponse[ident]

        return when {
            identResponse == null -> {
                val errorMessage = "Lookup gjeldende identer feilet"
                LOG.error(errorMessage)
                Either.Left(errorMessage)
            }
            identResponse.identer.isNullOrEmpty() -> {

                var errorMessage = "Lookup gjeldende identer feilet med feilmelding ${identResponse.feilmelding}"
                if (identResponse.feilmelding == NO_IDENT_ERROR_MSG) {
                    LOG.warn(errorMessage)
                    errorMessage = identResponse.feilmelding
                } else {
                    LOG.error(errorMessage)
                }
                Either.Left(errorMessage)
            }
            else -> {
                val identer = identResponse.identer
                Either.Right(
                    identer.map {
                        Ident(
                            it.ident,
                            IdentType.valueOf(it.identgruppe)
                        )
                    }
                )
            }
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

    companion object {
        private val LOG = LoggerFactory.getLogger(AktorregisterClient::class.java)
    }
}

enum class IdentType {
    AktoerId, NorskIdent
}

data class Ident(
    val ident: String,
    val type: IdentType
)
