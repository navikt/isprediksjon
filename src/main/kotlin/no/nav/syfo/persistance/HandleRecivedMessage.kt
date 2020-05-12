package no.nav.syfo.persistance

import io.ktor.util.KtorExperimentalAPI
import no.nav.syfo.Environment
import no.nav.syfo.log
import org.apache.kafka.clients.consumer.ConsumerRecord

@KtorExperimentalAPI
fun handleRecivedMessage(
    env: Environment,
    consumerRecord: ConsumerRecord<String, String>
) {
    try {
        when (consumerRecord.topic()) {
            env.sm2013ManuellBehandlingTopic -> log.info("Recieved message from ${env.sm2013ManuellBehandlingTopic}")
            env.sm2013AutomatiskBehandlingTopic -> log.info("Recieved message from ${env.sm2013AutomatiskBehandlingTopic}")
            env.smregisterRecievedSykmeldingBackupTopic -> log.info("Recieved message from ${env.smregisterRecievedSykmeldingBackupTopic}")
            env.sm2013BehandlingsutfallTopic -> log.info("Recieved message from ${env.sm2013BehandlingsutfallTopic}")
            env.smregisterBehandlingsutfallBackupTopic -> log.info("Recieved message from ${env.smregisterBehandlingsutfallBackupTopic}")
            env.syfoSykmeldingstatusLeesahTopic -> log.info("Recieved message from ${env.syfoSykmeldingstatusLeesahTopic}")
            env.syfoRegisterStatusBackupTopic -> log.info("Recieved message from ${env.syfoRegisterStatusBackupTopic}")
        }
    } catch (e: Exception) {
        log.error("Noe feilet! :( ${e.message}")
    }
}
