package no.nav.syfo.persistence

import io.ktor.util.KtorExperimentalAPI
import no.nav.syfo.Environment
import no.nav.syfo.database.Database
import no.nav.syfo.log
import no.nav.syfo.persistence.db.*
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.TopicPartition

@KtorExperimentalAPI
fun handleReceivedMessage(
    database: Database,
    env: Environment,
    consumerRecord: ConsumerRecord<String, String>
) {
    if (consumerRecord.value() == null) {
        log.warn("ConsumerRecord.value() er null, det er nok en tombstone! key ${consumerRecord.key()} fra topic: ${consumerRecord.topic()}, for partition ${consumerRecord.partition()} with offset ${consumerRecord.offset()} Ta kontakt med eier av topic hvis du tror dette er en feil!")
        return
    }
    try {
        when (consumerRecord.topic()) {
            env.sm2013ManuellBehandlingTopic -> {
                database.createSmManuellBehandling(consumerRecord.value(), consumerRecord.key())
            }
            env.sm2013AutomatiskBehandlingTopic -> {
                database.createSmAutomatiskBehandling(consumerRecord.value(), consumerRecord.key())
            }
            env.smregisterRecievedSykmeldingBackupTopic -> {
                database.createSmHist(consumerRecord.value(), consumerRecord.key())
            }
            env.sm2013BehandlingsutfallTopic -> {
                database.createSmBehandlingsutfall(consumerRecord.value(), consumerRecord.key())
            }
            env.smregisterBehandlingsutfallBackupTopic -> {
                database.createSmBehandlingsutfallHist(consumerRecord.value(), consumerRecord.key())
            }
            env.syfoSykmeldingstatusLeesahTopic -> {
                database.createSmSykmeldingstatus(consumerRecord.value(), consumerRecord.key())
            }
            env.syfoRegisterStatusBackupTopic -> {
                database.createSmSykmeldingstatusHist(consumerRecord.value(), consumerRecord.key())
            }
        }
    } catch (e: Exception) {
        log.error("Noe feilet! :( Skulle lese data med key ${consumerRecord.key()} fra topic: ${consumerRecord.topic()}, for partition ${consumerRecord.partition()} with offset ${consumerRecord.offset()}. ${e.message}")
        throw e
    }
}
