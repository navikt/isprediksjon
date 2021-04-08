package no.nav.syfo.auth

import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Paths

private val log: Logger = LoggerFactory.getLogger("no.nav.syfo.application.api")

private val objectMapper: ObjectMapper = ObjectMapper().apply {
    registerKotlinModule()
    registerModule(JavaTimeModule())
}

data class Tilganger(
    val identer: List<String>
)

private fun lesTilgangsfil(path: String): Tilganger {
    log.info("Leser tilgangsfil fra $path")
    val s = Paths.get(path).toFile().readText()
    return objectMapper.readValue<Tilganger>(s).also { log.info("Leste tilgang fra fil med ${it.identer.size} identer") }
}

private const val vaultFile = "/var/run/secrets/nais.io/vault/tilgang.json"

class MidlertidigTilgangsSjekk(pathTilTilgangsfil: String = vaultFile) {

    var tilgangListe = arrayListOf<String>()

    init {
        val tilgangsFil = lesTilgangsfil(pathTilTilgangsfil)
        tilgangListe.addAll(tilgangsFil.identer)
    }

    fun harTilgang(navIdent: String): Boolean = tilgangListe.contains(navIdent.toUpperCase())
}
