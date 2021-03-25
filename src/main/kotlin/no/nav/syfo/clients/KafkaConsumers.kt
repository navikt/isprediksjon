package no.nav.syfo.clients

import no.nav.syfo.Environment
import no.nav.syfo.VaultSecrets
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import java.util.*

fun kafkaConsumerSmregProperties(
    env: Environment,
    vaultSecrets: VaultSecrets
) = Properties().apply {
    this[ConsumerConfig.GROUP_ID_CONFIG] = "${env.applicationName}-consumer4"
    this[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
    this[ConsumerConfig.MAX_POLL_RECORDS_CONFIG] = "10000"
    this[ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG] = "" + (10 * 1024 * 1024)
    this[CommonClientConfigs.RETRIES_CONFIG] = "2"
    this[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = "false"
    this["security.protocol"] = "SASL_SSL"
    this["sasl.mechanism"] = "PLAIN"
    this["schema.registry.url"] = "http://kafka-schema-registry.tpa:8081"
    this[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = "org.apache.kafka.common.serialization.StringDeserializer"
    this[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = "org.apache.kafka.common.serialization.StringDeserializer"

    this["sasl.jaas.config"] = "org.apache.kafka.common.security.plain.PlainLoginModule required " +
        "username=\"${vaultSecrets.serviceuserUsername}\" password=\"${vaultSecrets.serviceuserPassword}\";"
    this[CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG] = env.kafkaBootstrapServers
}

fun kafkaConsumerOppfolgingstilfelleProperties(
    env: Environment,
    vaultSecrets: VaultSecrets
) = Properties().apply {
    this[ConsumerConfig.GROUP_ID_CONFIG] = "${env.applicationName}-consumer-tilfelle-1"
    this[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "latest"
    this[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = true
    this[ConsumerConfig.MAX_POLL_RECORDS_CONFIG] = "10"
    this[CommonClientConfigs.RETRIES_CONFIG] = "2"
    this[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = "false"
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
    val kafkaConsumerOppfolgingstilfelle = KafkaConsumer<String, String>(kafkaConsumerOppfolgingstilfelleProperties(env, vaultSecrets))
    val kafkaConsumerSmReg = KafkaConsumer<String, String>(kafkaConsumerSmregProperties(env, vaultSecrets))
}
