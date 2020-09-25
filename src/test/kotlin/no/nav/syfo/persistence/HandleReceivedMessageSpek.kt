package no.nav.syfo.persistence

import io.ktor.server.testing.*
import io.ktor.util.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.launch
import no.nav.common.KafkaEnvironment
import no.nav.syfo.application.ApplicationState
import no.nav.syfo.blockingApplicationLogic
import no.nav.syfo.clients.kafkaConsumerProperties
import org.amshove.kluent.shouldBeEqualTo
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.TopicPartition
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import testutil.*
import java.time.Duration

@InternalAPI
object HandleReceivedMessageSpek : Spek({
    val topicName = "topic1"

    val embeddedEnvironment = KafkaEnvironment(
        autoStart = false,
        withSchemaRegistry = false,
        topicNames = listOf(topicName)
    )

    val env = testEnvironment(
        getRandomPort(),
        embeddedEnvironment.brokersURL
    )

    val database = TestDB()

    beforeGroup {
        embeddedEnvironment.start()
    }

    afterGroup {
        embeddedEnvironment.tearDown()
        database.stop()
        unmockkAll()
    }

    describe("HandleReceivedMessage") {

        with(TestApplicationEngine()) {
            start()

            val consumerPropertiesOversikthendelse = kafkaConsumerProperties(env, vaultSecrets)
                .overrideForTest()
                .apply {
                    put("specific.avro.reader", false)
                    put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
                    put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
                }
            val consumerOversikthendelse = KafkaConsumer<String, String>(consumerPropertiesOversikthendelse)
            consumerOversikthendelse.subscribe(listOf(topicName))

            val partition = 0
            val sykmeldingTopicPartition = TopicPartition(env.sm2013ManuellBehandlingTopic, partition)

            afterEachTest {
                database.connection.dropData()
            }

            describe("Read and store message from ${env.sm2013ManuellBehandlingTopic}") {
                val sykmeldingData = "sykmelding"
                val sykmeldingRecord = ConsumerRecord(
                    env.sm2013ManuellBehandlingTopic,
                    partition,
                    1,
                    "something",
                    sykmeldingData
                )

                val mockConsumer = mockk<KafkaConsumer<String, String>>()
                every { mockConsumer.poll(Duration.ofMillis(0)) } returns ConsumerRecords(
                    mapOf(sykmeldingTopicPartition to listOf(sykmeldingRecord))
                )

                it("should store") {
                    launch {
                        blockingApplicationLogic(
                            applicationState = ApplicationState(
                                alive = true,
                                ready = true
                            ),
                            database = database,
                            env = env,
                            kafkaConsumer = mockConsumer
                        )
                    }

                    val sykmeldingRecordId = sykmeldingRecord.key()

                    val sykmeldingListe: List<Any> = database.connection.getSmManuellBehandling(sykmeldingRecordId)

                    sykmeldingListe.size shouldBeEqualTo 1

                    val sykmeldingStoredId = sykmeldingListe.first()

                    sykmeldingStoredId shouldBeEqualTo sykmeldingRecordId
                }
            }
        }
    }
})
