package no.nav.syfo.persistence

import io.ktor.server.testing.*
import io.ktor.util.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import no.nav.common.KafkaEnvironment
import no.nav.syfo.kafka.kafkaConsumerSmregProperties
import no.nav.syfo.kafka.pollAndProcessSMRegTopic
import org.amshove.kluent.shouldBeEqualTo
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.TopicPartition
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import testutil.*
import testutil.generator.generateSykmeldingRecord
import java.time.Duration

@InternalAPI
object HandleReceivedMessageSpek : Spek({
    data class TopicTable(
        val topic: String,
        val table: String
    )

    val sm2013ManuellBehandlingTopic = "topic1"
    val sm2013AutomatiskBehandlingTopic = "topic2"
    val smregisterRecievedSykmeldingBackupTopic = "topic3"
    val sm2013BehandlingsutfallTopic = "topic4"
    val smregisterBehandlingsutfallBackupTopic = "topic5"
    val syfoSykmeldingstatusLeesahTopic = "topic6"
    val syfoRegisterStatusBackupTopic = "topic7"

    val topicTableList = listOf(
        TopicTable(sm2013ManuellBehandlingTopic, "smManuellBehandling"),
        TopicTable(sm2013AutomatiskBehandlingTopic, "smAutomatiskBehandling"),
        TopicTable(smregisterRecievedSykmeldingBackupTopic, "smHist"),
        TopicTable(sm2013BehandlingsutfallTopic, "smBehandlingsutfall"),
        TopicTable(smregisterBehandlingsutfallBackupTopic, "smBehandlingsutfallHist"),
        TopicTable(syfoSykmeldingstatusLeesahTopic, "smSykmeldingstatus"),
        TopicTable(syfoRegisterStatusBackupTopic, "smSykmeldingstatusHist")
    )
    val topicList = topicTableList.map {
        it.topic
    }

    val embeddedEnvironment = KafkaEnvironment(
        autoStart = false,
        withSchemaRegistry = false,
        topicNames = topicList
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

            val consumerPropertiesOversikthendelse = kafkaConsumerSmregProperties(env, testVaultSecrets)
                .overrideForTest()
                .apply {
                    put("specific.avro.reader", false)
                    put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
                    put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
                }
            val kafkaConsumerSykmelding = KafkaConsumer<String, String>(consumerPropertiesOversikthendelse)
            kafkaConsumerSykmelding.subscribe(topicList)

            val partition = 0
            val sykmeldingTopicPartition = TopicPartition(env.sm2013AutomatiskBehandlingTopic, partition)

            topicTableList.forEach {
                describe("Read and store message from ${it.topic}") {
                    val table = it.table
                    afterEachTest {
                        database.connection.dropData(table)
                    }

                    val sykmeldingRecord = generateSykmeldingRecord(it.topic)

                    val mockConsumer = mockk<KafkaConsumer<String, String>>()
                    every { mockConsumer.poll(any<Duration>()) } returns ConsumerRecords(
                        mapOf(sykmeldingTopicPartition to listOf(sykmeldingRecord))
                    )
                    every { mockConsumer.commitSync() } returns Unit

                    it("should store data from record") {
                        runBlocking { pollAndProcessSMRegTopic(mockConsumer, database, env) }
                        verify(exactly = 1) { mockConsumer.commitSync() }
                        val sykmeldingListe: List<String> = database.connection.getSM(table, sykmeldingRecord.key())
                        sykmeldingListe.size shouldBeEqualTo 1
                    }
                }
            }
        }
    }
})
