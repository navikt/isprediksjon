package no.nav.syfo.util

import no.nav.syfo.prediksjon.PrediksjonOutput

fun List<PrediksjonOutput>.latestPrediksjon(): PrediksjonOutput {
    return sortedWith(compareByDescending { it.prediksjonCreated }).first()
}
