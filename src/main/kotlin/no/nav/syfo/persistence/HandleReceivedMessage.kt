package no.nav.syfo.persistence

import io.ktor.util.KtorExperimentalAPI
import no.nav.syfo.Environment
import no.nav.syfo.database.Database
import no.nav.syfo.log
import org.apache.kafka.clients.consumer.ConsumerRecord

@KtorExperimentalAPI
fun handleReceivedMessage(
    database: Database,
    env: Environment,
    consumerRecord: ConsumerRecord<String, String>
) {
    try {
        when (consumerRecord.topic()) {
            env.sm2013ManuellBehandlingTopic -> log.info("Recieved message from ${env.sm2013ManuellBehandlingTopic} with key ${consumerRecord.key()}")
            env.sm2013AutomatiskBehandlingTopic -> log.info("Recieved message from ${env.sm2013AutomatiskBehandlingTopic} with key ${consumerRecord.key()}")
            env.smregisterRecievedSykmeldingBackupTopic -> log.info("Recieved message from ${env.smregisterRecievedSykmeldingBackupTopic} with key ${consumerRecord.key()}")
            env.sm2013BehandlingsutfallTopic -> log.info("Recieved message from ${env.sm2013BehandlingsutfallTopic} with key ${consumerRecord.key()}")
            env.smregisterBehandlingsutfallBackupTopic -> log.info("Recieved message from ${env.smregisterBehandlingsutfallBackupTopic} with key ${consumerRecord.key()}")
            env.syfoSykmeldingstatusLeesahTopic -> log.info("Recieved message from ${env.syfoSykmeldingstatusLeesahTopic} with key ${consumerRecord.key()}")
            env.syfoRegisterStatusBackupTopic -> log.info("Recieved message from ${env.syfoRegisterStatusBackupTopic} with key ${consumerRecord.key()}")
        }
    } catch (e: Exception) {
        log.error("Noe feilet! :( Skulle lese data med key ${consumerRecord.key()} fra topic: ${consumerRecord.topic()} ${e.message}")
        throw e
    }
}
