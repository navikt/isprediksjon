package no.nav.syfo.domain

private val thirteenDigits = Regex("\\d{13}")

data class AktorId(val value: String) {
    init {
        if (!thirteenDigits.matches(value)) {
            throw IllegalArgumentException("$value is not a valid aktorid")
        }
    }
}
