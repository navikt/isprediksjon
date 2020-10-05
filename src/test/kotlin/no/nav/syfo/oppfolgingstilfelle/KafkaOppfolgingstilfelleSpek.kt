package no.nav.syfo.oppfolgingstilfelle

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.server.testing.*
import io.ktor.util.*
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.common.KafkaEnvironment
import no.nav.syfo.clients.aktor.AktorService
import no.nav.syfo.clients.aktor.AktorregisterClient
import no.nav.syfo.clients.kafkaConsumerProperties
import no.nav.syfo.clients.sts.StsRestClient
import no.nav.syfo.clients.syketilfelle.SyketilfelleClient
import no.nav.syfo.pollAndProcessOppfolgingstilfelleTopic
import no.nav.syfo.prediksjon.PrediksjonInputService
import org.amshove.kluent.shouldBeEqualTo
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.TopicPartition
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import testutil.*
import testutil.UserConstants.ARBEIDSTAKER_FNR
import testutil.generator.generateKOppfolgingstilfellePeker
import testutil.mock.AktorregisterMock
import testutil.mock.StsRestMock
import testutil.mock.SyketilfelleMock
import java.time.Duration

@InternalAPI
object KafkaOppfolgingstilfelleSpek : Spek({

    with(TestApplicationEngine()) {
        start()

        val oppfolgingstilfelleTopic = "oppfolgingstilfelle"
        val embeddedEnvironment = KafkaEnvironment(
            autoStart = false,
            withSchemaRegistry = false,
            topicNames = listOf(oppfolgingstilfelleTopic)
        )
        val env = testEnvironment(
            getRandomPort(),
            embeddedEnvironment.brokersURL
        )

        val vaultSecrets = vaultSecrets

        val database = TestDB()

        val stsRestMock = StsRestMock()
        val stsRestClient = StsRestClient(
            baseUrl = stsRestMock.url,
            serviceuserUsername = vaultSecrets.serviceuserUsername,
            serviceuserPassword = vaultSecrets.serviceuserPassword
        )

        val aktorregisterMock = AktorregisterMock()
        val aktorregisterClient = AktorregisterClient(
            baseUrl = aktorregisterMock.url,
            stsRestClient = stsRestClient
        )
        val aktorService = AktorService(aktorregisterClient)

        val syketilfelleMock = SyketilfelleMock()
        val syketilfelleClient = SyketilfelleClient(
            baseUrl = syketilfelleMock.url,
            stsRestClient = stsRestClient
        )

        val prediksjonInputService = PrediksjonInputService(database)

        val oppfolgingstilfelleService = OppfolgingstilfelleService(
            aktorService,
            prediksjonInputService,
            syketilfelleClient
        )

        beforeGroup {
            embeddedEnvironment.start()

            aktorregisterMock.server.start()
            stsRestMock.server.start()
            syketilfelleMock.server.start()
        }

        afterGroup {
            embeddedEnvironment.tearDown()

            database.stop()
            aktorregisterMock.server.stop(1L, 10L)
            stsRestMock.server.stop(1L, 10L)
            syketilfelleMock.server.stop(1L, 10L)
        }

        afterEachTest {
            val table = "prediksjon_input"
            database.connection.dropData(table)
        }

        describe("Read and store PPrediksjonInput") {
            val consumerPropertiesOppfolgingstilfelle = kafkaConsumerProperties(env, testutil.vaultSecrets)
                .overrideForTest()

            val kafkaConsumerOppfolgingstilfelle = KafkaConsumer<String, String>(consumerPropertiesOppfolgingstilfelle)
            kafkaConsumerOppfolgingstilfelle.subscribe(listOf(oppfolgingstilfelleTopic))

            val kOppfolgingstilfellePeker = generateKOppfolgingstilfellePeker
            val kOppfolgingstilfellePerson = syketilfelleMock.kOppfolgingstilfellePerson

            val partition = 0
            val oppfolgingstilfelleTopicPartition = TopicPartition(oppfolgingstilfelleTopic, partition)

            it("should store PrediksjonInput based on OppfolgingstilfellePeker") {
                val kOppfolgingstilfellePekerJson = objectMapper.writeValueAsString(kOppfolgingstilfellePeker)
                val oppfolgingstilfellePekerRecord = ConsumerRecord(
                    oppfolgingstilfelleTopic,
                    partition,
                    1,
                    "something",
                    kOppfolgingstilfellePekerJson
                )

                val mockConsumer = mockk<KafkaConsumer<String, String>>()
                every { mockConsumer.poll(Duration.ofMillis(0)) } returns ConsumerRecords(
                    mapOf(oppfolgingstilfelleTopicPartition to listOf(oppfolgingstilfellePekerRecord))
                )

                runBlocking {
                    pollAndProcessOppfolgingstilfelleTopic(
                        kafkaConsumer = mockConsumer,
                        oppfolgingstilfelleService = oppfolgingstilfelleService,
                        isProcessOppfolgingstilfelleOn = true
                    )
                }

                val prediksjonInputFnrList =
                    database.connection.getPrediksjonInput(ARBEIDSTAKER_FNR)

                prediksjonInputFnrList.size shouldBeEqualTo 1

                val returnedPrediksjonInput = prediksjonInputFnrList.first()

                returnedPrediksjonInput.fnr shouldBeEqualTo ARBEIDSTAKER_FNR.value
                returnedPrediksjonInput.aktorId shouldBeEqualTo kOppfolgingstilfellePeker.aktorId
                returnedPrediksjonInput.tilfelleStartDate shouldBeEqualTo kOppfolgingstilfellePerson.tidslinje.first().dag
                returnedPrediksjonInput.tilfelleEndDate shouldBeEqualTo kOppfolgingstilfellePerson.tidslinje.last().dag
            }
        }
    }
})

private val objectMapper: ObjectMapper = ObjectMapper().apply {
    registerKotlinModule()
    registerModule(JavaTimeModule())
    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
}
