package no.nav.syfo.clients

import no.nav.syfo.Environment
import no.nav.syfo.VaultSecrets
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import java.util.*

fun kafkaConsumerProperties(
    env: Environment,
    vaultSecrets: VaultSecrets
) = Properties().apply {
    this[ConsumerConfig.GROUP_ID_CONFIG] = "${env.applicationName}-consumer2"
    this[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
    this[CommonClientConfigs.RETRIES_CONFIG] = "2"
    this["acks"] = "all"
    this["security.protocol"] = "SASL_SSL"
    this["sasl.mechanism"] = "PLAIN"
    this["schema.registry.url"] = "http://kafka-schema-registry.tpa:8081"
    this[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = "org.apache.kafka.common.serialization.StringDeserializer"
    this[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = "org.apache.kafka.common.serialization.StringDeserializer"

    this["sasl.jaas.config"] = "org.apache.kafka.common.security.plain.PlainLoginModule required " +
        "username=\"${vaultSecrets.serviceuserUsername}\" password=\"${vaultSecrets.serviceuserPassword}\";"
    this[CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG] = env.kafkaBootstrapServers
}

class KafkaConsumers(
    env: Environment,
    vaultSecrets: VaultSecrets
) {
    val kafkaConsumerSmReg = KafkaConsumer<String, String>(kafkaConsumerProperties(env, vaultSecrets))
}
