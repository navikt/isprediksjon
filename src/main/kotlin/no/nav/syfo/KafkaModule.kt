package no.nav.syfo

import io.ktor.application.*
import io.ktor.util.*
import kotlinx.coroutines.*
import net.logstash.logback.argument.StructuredArguments
import no.nav.syfo.application.ApplicationState
import no.nav.syfo.clients.KafkaConsumers
import no.nav.syfo.database.DatabaseInterface
import no.nav.syfo.persistence.handleReceivedMessage
import org.apache.kafka.clients.consumer.KafkaConsumer
import java.time.Duration

fun Application.kafkaModule(
    applicationState: ApplicationState,
    env: Environment,
    vaultSecrets: VaultSecrets
) {
    val kafkaConsumers = KafkaConsumers(env, vaultSecrets)

    launch(backgroundTasksContext) {
        launchListeners(
            applicationState,
            database,
            env,
            kafkaConsumers
        )
    }
}

@KtorExperimentalAPI
suspend fun launchListeners(
    applicationState: ApplicationState,
    database: DatabaseInterface,
    env: Environment,
    kafkaConsumers: KafkaConsumers
) {
    createListener(applicationState) {
        val kafkaConsumerSmReg = kafkaConsumers.kafkaConsumerSmReg

        log.info("Subscribing to topics: ${env.kafkaConsumerTopics}")
        kafkaConsumerSmReg.subscribe(env.kafkaConsumerTopics)
        blockingApplicationLogic(
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
suspend fun blockingApplicationLogic(
    applicationState: ApplicationState,
    database: DatabaseInterface,
    env: Environment,
    kafkaConsumer: KafkaConsumer<String, String>
) {
    while (applicationState.ready) {
        kafkaConsumer.poll(Duration.ofMillis(0)).forEach { consumerRecord ->
            handleReceivedMessage(database, env, consumerRecord)
        }
        delay(100)
    }
}
