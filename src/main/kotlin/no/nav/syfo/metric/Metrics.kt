package no.nav.syfo.metric

import io.prometheus.client.Counter

const val METRICS_NS = "isprediksjon"

const val CALL_SYKETILFELLE_OPPFOLGINGSTILFELLE_AKTOR_EMPTY = "call_syketilfelle_oppfolgingstilfelle_aktorid_empty_count"
val COUNT_CALL_SYKETILFELLE_OPPFOLGINGSTILFELLE_AKTOR_EMPTY: Counter = Counter.build()
    .namespace(METRICS_NS)
    .name(CALL_SYKETILFELLE_OPPFOLGINGSTILFELLE_AKTOR_EMPTY)
    .help("Counts the number of responses from syfosyketilfelle with status 204 received")
    .register()
const val CALL_SYKETILFELLE_OPPFOLGINGSTILFELLE_AKTOR_SUCCESS = "call_syketilfelle_oppfolgingstilfelle_aktorid_success_count"
val COUNT_CALL_SYKETILFELLE_OPPFOLGINGSTILFELLE_AKTOR_SUCCESS: Counter = Counter.build()
    .namespace(METRICS_NS)
    .name(CALL_SYKETILFELLE_OPPFOLGINGSTILFELLE_AKTOR_SUCCESS)
    .help("Counts the number of responses from syfosyketilfelle with status 204 received")
    .register()
const val CALL_SYKETILFELLE_OPPFOLGINGSTILFELLE_AKTOR_FAIL = "call_syketilfelle_oppfolgingstilfelle_aktorid_fail_count"
val COUNT_CALL_SYKETILFELLE_OPPFOLGINGSTILFELLE_AKTOR_FAIL: Counter = Counter.build()
    .namespace(METRICS_NS)
    .name(CALL_SYKETILFELLE_OPPFOLGINGSTILFELLE_AKTOR_FAIL)
    .help("Counts the number of responses from syfosyketilfelle with status 204 received")
    .register()

const val OPPFOLGINGSTILFELLE_SKIPPED_FODSELSNUMMER = "oppfolgingstilfelle_skipped_fodselsnummer_count"
val COUNT_OPPFOLGINGSTILFELLE_SKIPPED_FODSELSNUMMER: Counter = Counter.build()
    .namespace(METRICS_NS)
    .name(OPPFOLGINGSTILFELLE_SKIPPED_FODSELSNUMMER)
    .help("Counts the number of Oppfolgingstilfeller skipped because Fodselsnummer was not found")
    .register()

const val OPPFOLGINGSTILFELLE_SKIPPED_OPPFOLGINGSTILFELLE_ = "oppfolgingstilfelle_skipped_oppfolgingstilfelle_count"
val COUNT_OPPFOLGINGSTILFELLE_SKIPPED_OPPFOLGINGSTILFELLE: Counter = Counter.build()
    .namespace(METRICS_NS)
    .name(OPPFOLGINGSTILFELLE_SKIPPED_OPPFOLGINGSTILFELLE_)
    .help("Counts the number of Oppfolgingstilfeller skipped because Oppfolgingstilfelle was not found")
    .register()

const val PREDIKSJON_INPUT_CREATED = "prediksjon_input_created_count"
val COUNT_PREDIKSJON_INPUT_CREATED: Counter = Counter.build()
    .namespace(METRICS_NS)
    .name(PREDIKSJON_INPUT_CREATED)
    .help("Counts the number of PrediksjonInput stored in database")
    .register()