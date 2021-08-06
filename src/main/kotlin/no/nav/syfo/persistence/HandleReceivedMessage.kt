package no.nav.syfo.persistence

import io.ktor.util.*
import no.nav.syfo.application.Environment
import no.nav.syfo.log
import no.nav.syfo.persistence.db.*
import org.apache.kafka.clients.consumer.ConsumerRecord
import java.sql.Connection

@KtorExperimentalAPI
fun handleReceivedMessage(
    connection: Connection,
    env: Environment,
    consumerRecord: ConsumerRecord<String, String>
) {
    if (consumerRecord.value() == null) {
        log.warn("ConsumerRecord.value() er null, det er nok en tombstone! key ${consumerRecord.key()} fra topic: ${consumerRecord.topic()}, for partition ${consumerRecord.partition()} with offset ${consumerRecord.offset()} Ta kontakt med eier av topic hvis du tror dette er en feil!")
        return
    }
    try {
        val uniqueId = consumerRecord.key() + consumerRecord.timestamp()
        connection.createSmRegRow(env, consumerRecord.topic(), uniqueId, consumerRecord.value(), consumerRecord.key())
    } catch (e: Exception) {
        log.error("Noe feilet! :( Skulle lese data med key ${consumerRecord.key()} fra topic: ${consumerRecord.topic()}, for partition ${consumerRecord.partition()} with offset ${consumerRecord.offset()}. ${e.message}")
        throw e
    }
}
