package no.nav.syfo

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.application.*
import io.ktor.util.*
import kotlinx.coroutines.*
import net.logstash.logback.argument.StructuredArguments
import no.nav.syfo.application.ApplicationState
import no.nav.syfo.clients.KafkaConsumers
import no.nav.syfo.clients.aktor.AktorService
import no.nav.syfo.clients.aktor.AktorregisterClient
import no.nav.syfo.clients.sts.StsRestClient
import no.nav.syfo.clients.syketilfelle.SyketilfelleClient
import no.nav.syfo.database.DatabaseInterface
import no.nav.syfo.metric.HISTOGRAM_OPPFOLGINGSTILFELLE_DURATION
import no.nav.syfo.oppfolgingstilfelle.OppfolgingstilfelleService
import no.nav.syfo.oppfolgingstilfelle.domain.KOppfolgingstilfellePeker
import no.nav.syfo.persistence.handleReceivedMessage
import no.nav.syfo.prediksjon.PrediksjonInputService
import no.nav.syfo.util.callIdArgument
import no.nav.syfo.util.kafkaCallIdOppfolgingstilfelle
import org.apache.kafka.clients.consumer.KafkaConsumer
import java.time.Duration

fun Application.kafkaModule(
    applicationState: ApplicationState,
    env: Environment,
    vaultSecrets: VaultSecrets
) {
    log.info("Initialization of kafka module starting")

    val stsRestClient = StsRestClient(
        baseUrl = env.stsRestUrl,
        serviceuserUsername = vaultSecrets.serviceuserUsername,
        serviceuserPassword = vaultSecrets.serviceuserPassword
    )
    val aktorregisterClient = AktorregisterClient(
        baseUrl = env.aktorregisterV1Url,
        stsRestClient = stsRestClient
    )
    val aktorService = AktorService(aktorregisterClient)
    val prediksjonInputService = PrediksjonInputService(database)
    val syketilfelleClient = SyketilfelleClient(
        baseUrl = env.syketilfelleUrl,
        stsRestClient = stsRestClient
    )
    val oppfolgingstilfelleService = OppfolgingstilfelleService(
        aktorService = aktorService,
        prediksjonInputService = prediksjonInputService,
        syketilfelleClient = syketilfelleClient
    )

    val kafkaConsumers = KafkaConsumers(env, vaultSecrets)

    launch(backgroundTasksContext) {
        launchListeners(
            applicationState,
            database,
            env,
            kafkaConsumers,
            oppfolgingstilfelleService
        )
    }
    log.info("Initialization of kafka module done")
}

@KtorExperimentalAPI
suspend fun launchListeners(
    applicationState: ApplicationState,
    database: DatabaseInterface,
    env: Environment,
    kafkaConsumers: KafkaConsumers,
    oppfolgingstilfelleService: OppfolgingstilfelleService
) {
    createListener(applicationState) {
        val kafkaConsumerOppfolgingstilfelle = kafkaConsumers.kafkaConsumerOppfolgingstilfelle

        val oppfolgingstilfelleTopic = env.oppfolgingstilfelleTopic
        log.info("Subscribing to topic: $oppfolgingstilfelleTopic")
        kafkaConsumerOppfolgingstilfelle.subscribe(listOf(oppfolgingstilfelleTopic))
        blockingApplicationLogicOppfolgingstilfelle(
            applicationState,
            kafkaConsumerOppfolgingstilfelle,
            oppfolgingstilfelleService,
            env.isProcessOppfolgingstilfelleOn
        )
    }

    createListener(applicationState) {
        val kafkaConsumerSmReg = kafkaConsumers.kafkaConsumerSmReg

        log.info("Subscribing to topics: ${env.kafkaConsumerTopics}")
        kafkaConsumerSmReg.subscribe(env.kafkaConsumerTopics)
        blockingApplicationLogicSmRegTopic(
            applicationState,
            database,
            env,
            kafkaConsumerSmReg
        )
    }
}

suspend fun createListener(applicationState: ApplicationState, action: suspend CoroutineScope.() -> Unit): Job =
    GlobalScope.launch {
        try {
            action()
        } catch (e: Exception) {
            log.error(
                "En uhåndtert feil oppstod, applikasjonen restarter {}",
                StructuredArguments.fields(e.message),
                e.cause
            )
        } finally {
            applicationState.alive = false
        }
    }

@KtorExperimentalAPI
fun blockingApplicationLogicSmRegTopic(
    applicationState: ApplicationState,
    database: DatabaseInterface,
    env: Environment,
    kafkaConsumer: KafkaConsumer<String, String>
) {
    while (applicationState.ready) {
        pollAndProcessSMRegTopic(kafkaConsumer, database, env)
    }
}

@KtorExperimentalAPI
fun pollAndProcessSMRegTopic(
    kafkaConsumer: KafkaConsumer<String, String>,
    database: DatabaseInterface,
    env: Environment
) {
    val starttime = System.currentTimeMillis()
    val consumerRecords = kafkaConsumer.poll(Duration.ofMillis(1000))
    val numberOfRecords = consumerRecords.count()
    if (numberOfRecords > 0) {
        database.connection.use {
            consumerRecords.forEach { consumerRecord ->
                handleReceivedMessage(it, env, consumerRecord)
            }
            it.commit()
        }
        kafkaConsumer.commitSync()
        log.info("Consumed $numberOfRecords records in " + (System.currentTimeMillis() - starttime) + " ms")
    }
}

@KtorExperimentalAPI
suspend fun blockingApplicationLogicOppfolgingstilfelle(
    applicationState: ApplicationState,
    kafkaConsumer: KafkaConsumer<String, String>,
    oppfolgingstilfelleService: OppfolgingstilfelleService,
    isProcessOppfolgingstilfelleOn: Boolean
) {
    while (applicationState.ready) {
        pollAndProcessOppfolgingstilfelleTopic(
            kafkaConsumer = kafkaConsumer,
            oppfolgingstilfelleService = oppfolgingstilfelleService,
            isProcessOppfolgingstilfelleOn = isProcessOppfolgingstilfelleOn
        )
    }
}

suspend fun pollAndProcessOppfolgingstilfelleTopic(
    kafkaConsumer: KafkaConsumer<String, String>,
    oppfolgingstilfelleService: OppfolgingstilfelleService,
    isProcessOppfolgingstilfelleOn: Boolean
) {
    var logValues = arrayOf(
        StructuredArguments.keyValue("id", "missing"),
        StructuredArguments.keyValue("timestamp", "missing")
    )

    val logKeys = logValues.joinToString(prefix = "(", postfix = ")", separator = ",") {
        "{}"
    }

    kafkaConsumer.poll(Duration.ofMillis(100)).forEach { consumerRecord ->
        val oppfolgingstilfelleTimer = HISTOGRAM_OPPFOLGINGSTILFELLE_DURATION.startTimer()

        val callId = kafkaCallIdOppfolgingstilfelle()
        val oppfolgingstilfellePeker: KOppfolgingstilfellePeker = objectMapper.readValue(consumerRecord.value())

        logValues = arrayOf(
            StructuredArguments.keyValue("id", consumerRecord.key()),
            StructuredArguments.keyValue("timestamp", consumerRecord.timestamp())
        )
        log.info(
            "Received KOppfolgingstilfellePeker, ready to process, $logKeys, {}",
            *logValues,
            callIdArgument(callId)
        )
        if (isProcessOppfolgingstilfelleOn) {
            oppfolgingstilfelleService.receiveOppfolgingstilfelle(oppfolgingstilfellePeker, callId)
        }
        oppfolgingstilfelleTimer.observeDuration()
    }
    kafkaConsumer.commitSync()
}

private val objectMapper: ObjectMapper = ObjectMapper().apply {
    registerKotlinModule()
    registerModule(JavaTimeModule())
    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
}
