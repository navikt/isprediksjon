package no.nav.syfo.prediksjon

import no.nav.syfo.database.DatabaseInterface
import no.nav.syfo.oppfolgingstilfelle.domain.PersonOppfolgingstilfelle

class PrediksjonInputService(
    private val database: DatabaseInterface
) {
    fun createPrediksjonInput(personOppfolgingstilfelle: PersonOppfolgingstilfelle) {
        database.createPrediksjonInput(personOppfolgingstilfelle)
    }
}
