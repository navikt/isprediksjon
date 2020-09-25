package testutil.generator

import org.apache.kafka.clients.consumer.ConsumerRecord

fun generateSykmeldingRecord(topic: String): ConsumerRecord<String, String> {
    return ConsumerRecord(
        topic,
        0,
        1,
        "2ac48dec-ff0a-11ea-adc1-0242ac120002",
        "{\"name\": \"Sykmelding\"}"
    )
}
