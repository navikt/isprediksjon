package no.nav.syfo.metric

import io.prometheus.client.Counter
import io.prometheus.client.Histogram

const val METRICS_NS = "isprediksjon"

const val TILGANGSKONTROLL_OK = "tilgangskontroll_ok"
const val TILGANGSKONTROLL_FAIL = "tilgangskontroll_fail"

const val CALL_SYKETILFELLE_OPPFOLGINGSTILFELLE_AKTOR_EMPTY =
    "call_syketilfelle_oppfolgingstilfelle_aktorid_empty_count"
val COUNT_CALL_SYKETILFELLE_OPPFOLGINGSTILFELLE_AKTOR_EMPTY: Counter = Counter.build()
    .namespace(METRICS_NS)
    .name(CALL_SYKETILFELLE_OPPFOLGINGSTILFELLE_AKTOR_EMPTY)
    .help("Counts the number of responses from syfosyketilfelle with status 204 received")
    .register()
const val CALL_SYKETILFELLE_OPPFOLGINGSTILFELLE_AKTOR_SUCCESS =
    "call_syketilfelle_oppfolgingstilfelle_aktorid_success_count"
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

const val ANTALL_SYKMELDINGER_MOTTATT = "sykmelding_mottatt_count"
val COUNT_ANTALL_SYKMELDINGER_MOTTATT: Counter = Counter.build()
    .namespace(METRICS_NS)
    .name(ANTALL_SYKMELDINGER_MOTTATT)
    .help("Counts the number of sykmeldinger read from Kafka")
    .register()

const val OPPFOLGINGSTILFELLE_DURATION = "oppfolgingstilfelle_duration_histogram"
val HISTOGRAM_OPPFOLGINGSTILFELLE_DURATION: Histogram = Histogram.build()
    .namespace(METRICS_NS)
    .name(OPPFOLGINGSTILFELLE_DURATION)
    .help("Measure the current time it takes to handle an event oppfolgingstilfelle ")
    .register()

val COUNT_TILGANGSKONTROLL_OK: Counter = Counter.build()
    .namespace(METRICS_NS)
    .name(TILGANGSKONTROLL_OK)
    .help("Counts the number of successful requests to tilgangskontroll")
    .register()

val COUNT_TILGANGSKONTROLL_FAIL: Counter = Counter.build()
    .namespace(METRICS_NS)
    .labelNames("status")
    .name(TILGANGSKONTROLL_FAIL)
    .help("Counts the number of failing requests to tilgangskontroll")
    .register()

const val TILGANGSKONTROLL_FORBIDDEN = "call_tilgangskontroll_person_forbidden_count"
val COUNT_TILGANGSKONTROLL_FORBIDDEN: Counter = Counter.build()
    .namespace(METRICS_NS)
    .name(TILGANGSKONTROLL_FORBIDDEN)
    .help("Counts the number of forbidden calls to syfo-tilgangskontroll - person")
    .register()

const val PREDIKSJON_GET_OUTPUT_SUCCESS = "prediksjon_output_success"
val COUNT_PREDIKSJON_OUTPUT_SUCCESS: Counter = Counter.build()
    .namespace(METRICS_NS)
    .name(PREDIKSJON_GET_OUTPUT_SUCCESS)
    .help("Counts the number of successful queries for prediksjon_output")
    .register()

const val PREDIKSJON_GET_OUTPUT_FAILED = "prediksjon_output_failed"
val COUNT_PREDIKSJON_OUTPUT_FAILED: Counter = Counter.build()
    .namespace(METRICS_NS)
    .name(PREDIKSJON_GET_OUTPUT_FAILED)
    .help("Counts the number of failed queries (bad request) for prediksjon_output")
    .register()

const val PREDIKSJON_GET_OUTPUT_ERROR = "prediksjon_output_error"
val COUNT_PREDIKSJON_OUTPUT_ERROR: Counter = Counter.build()
    .namespace(METRICS_NS)
    .name(PREDIKSJON_GET_OUTPUT_ERROR)
    .help("Counts the number of internal server errors for prediksjon_output")
    .register()

const val PREDIKSJON_OUTPUT_FORBIDDEN = "prediksjon_output_forbidden"
val COUNT_PREDIKSJON_OUTPUT_FORBIDDEN: Counter = Counter.build()
    .namespace(METRICS_NS)
    .name(PREDIKSJON_OUTPUT_FORBIDDEN)
    .help("Counts the number of forbidden status retrieves")
    .register()
