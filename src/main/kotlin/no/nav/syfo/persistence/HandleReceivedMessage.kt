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
    consumerRecord: ConsumerRecord<String, String>,
    endOffsets: Map<TopicPartition, Long>
) {
    try {
        when (consumerRecord.topic()) {
            env.sm2013ManuellBehandlingTopic -> {
                logIfDoneWithPartition(consumerRecord, endOffsets)
                database.createSmManuellBehandling(consumerRecord.value(), consumerRecord.key())
            }
            env.sm2013AutomatiskBehandlingTopic -> {
                logIfDoneWithPartition(consumerRecord, endOffsets)
                database.createSmAutomatiskBehandling(consumerRecord.value(), consumerRecord.key())
            }
            env.smregisterRecievedSykmeldingBackupTopic -> {
                logIfDoneWithPartition(consumerRecord, endOffsets)
                database.createSmHist(consumerRecord.value(), consumerRecord.key())
            }
            env.sm2013BehandlingsutfallTopic -> {
                logIfDoneWithPartition(consumerRecord, endOffsets)
                database.createSmBehandlingsutfall(consumerRecord.value(), consumerRecord.key())
            }
            env.smregisterBehandlingsutfallBackupTopic -> {
                logIfDoneWithPartition(consumerRecord, endOffsets)
                database.createSmBehandlingsutfallHist(consumerRecord.value(), consumerRecord.key())
            }
            env.syfoSykmeldingstatusLeesahTopic -> {
                logIfDoneWithPartition(consumerRecord, endOffsets)
                database.createSmSykmeldingstatus(consumerRecord.value(), consumerRecord.key())
            }
            env.syfoRegisterStatusBackupTopic -> {
                logIfDoneWithPartition(consumerRecord, endOffsets)
                database.createSmSykmeldingstatusHist(consumerRecord.value(), consumerRecord.key())
            }
        }
    } catch (e: Exception) {
        log.error("Noe feilet! :( Skulle lese data med key ${consumerRecord.key()} fra topic: ${consumerRecord.topic()}, for partition ${consumerRecord.partition()} with offset ${consumerRecord.offset()}. ${e.message}")
        throw e
    }
}

fun logIfDoneWithPartition(consumerRecord: ConsumerRecord<String, String>, endOffsets: Map<TopicPartition, Long>) {
    val topicPartition = TopicPartition(consumerRecord.topic(), consumerRecord.partition())
    val endForThisPartition = endOffsets[topicPartition]

    if(endForThisPartition != null && (endForThisPartition - 1) == consumerRecord.offset()) {
        log.info("Kafka-trace: Er på endOffset for ${consumerRecord.topic()}-${consumerRecord.partition()}, med offset ${consumerRecord.offset()}. Endoffsets: $endOffsets")
    }
}
