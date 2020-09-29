package no.nav.syfo.prediksjon

import no.nav.syfo.database.DatabaseInterface
import no.nav.syfo.prediksjon.input.PPrediksjonInput

class PrediksjonInputService(
    private val database: DatabaseInterface
) {
    fun createPrediksjonInput(pPrediksjonInput: PPrediksjonInput) {
        database.createPrediksjonInput(pPrediksjonInput)
    }
}
