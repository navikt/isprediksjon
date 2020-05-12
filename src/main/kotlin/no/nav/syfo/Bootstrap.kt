package no.nav.syfo

import io.ktor.util.InternalAPI
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.*
import net.logstash.logback.argument.StructuredArguments
import no.nav.syfo.application.ApplicationServer
import no.nav.syfo.application.ApplicationState
import no.nav.syfo.application.createApplicationEngine
import no.nav.syfo.clients.KafkaConsumers
import no.nav.syfo.util.getFileAsString
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration

val log: Logger = LoggerFactory.getLogger("no.nav.syfo.isprediksjon")

@InternalAPI
@KtorExperimentalAPI
fun main() {
    val env = Environment()
    val vaultSecrets = VaultSecrets(
        serviceuserPassword = getFileAsString("/secrets/serviceuser/password"),
        serviceuserUsername = getFileAsString("/secrets/serviceuser/username")
    )

    val applicationState = ApplicationState()

    val kafkaConsumers = KafkaConsumers(env, vaultSecrets)

    val applicationEngine = createApplicationEngine(
        env,
        applicationState
    )

    val applicationServer = ApplicationServer(applicationEngine, applicationState)
    applicationServer.start()

    applicationState.ready = true

    log.info("Hello from isprediksjon")

    launchListeners(
        applicationState,
        env,
        kafkaConsumers
    )
}

@InternalAPI
fun createListener(applicationState: ApplicationState, action: suspend CoroutineScope.() -> Unit): Job =
    GlobalScope.launch {
        try {
            action()
        } catch (e: Exception) {
            log.error(
                "En uhåndtert feil oppstod, applikasjonen restarter {}",
                StructuredArguments.fields(e.message), e.cause
            )
        } finally {
            applicationState.alive = false
        }
    }

@InternalAPI
@KtorExperimentalAPI
fun launchListeners(
    applicationState: ApplicationState,
    env: Environment,
    kafkaConsumers: KafkaConsumers
) {
    createListener(applicationState) {
        val kafkaConsumerSmReg = kafkaConsumers.kafkaConsumerSmReg

        applicationState.ready = true

        kafkaConsumerSmReg.subscribe(env.kafkatopics)
        blockingApplicationLogic(
            applicationState,
            kafkaConsumerSmReg
        )
    }
}

@KtorExperimentalAPI
suspend fun blockingApplicationLogic(
    applicationState: ApplicationState,
    kafkaConsumer: KafkaConsumer<String, String>
) {
    while (applicationState.ready) {
        kafkaConsumer.poll(Duration.ofMillis(0)).forEach { consumerRecord ->
            val recievedSmRegObjekt: String = consumerRecord.value()

            log.info("Mottok objekt fra kafka topic ${consumerRecord.topic()} med key ${consumerRecord.key()}")
        }
        delay(100)
    }
}
