package no.nav.syfo.oppfolgingstilfelle

import io.ktor.server.testing.*
import io.ktor.util.*
import kotlinx.coroutines.runBlocking
import no.nav.syfo.clients.aktor.AktorService
import no.nav.syfo.clients.aktor.AktorregisterClient
import no.nav.syfo.clients.sts.StsRestClient
import no.nav.syfo.clients.syketilfelle.SyketilfelleClient
import no.nav.syfo.metric.COUNT_OPPFOLGINGSTILFELLE_SKIPPED_FODSELSNUMMER
import no.nav.syfo.prediksjon.PrediksjonInputService
import org.amshove.kluent.shouldBeEqualTo
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import testutil.TestDB
import testutil.UserConstants.ARBEIDSTAKER_FNR
import testutil.dropData
import testutil.generator.generateKOppfolgingstilfellePeker
import testutil.generator.generateKOppfolgingstilfellePekerPersonFinnesIkke
import testutil.getPrediksjonInput
import testutil.mock.AktorregisterMock
import testutil.mock.StsRestMock
import testutil.mock.SyketilfelleMock
import testutil.testVaultSecrets

@InternalAPI
object OppfolgingstilfelleServiceSpek : Spek({

    with(TestApplicationEngine()) {
        start()

        val vaultSecrets = testVaultSecrets

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
            aktorregisterMock.server.start()
            stsRestMock.server.start()
            syketilfelleMock.server.start()
        }

        afterGroup {
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

            it("should store PrediksjonInput based on Oppfolgingstilfelle") {
                val kOppfolgingstilfellePeker = generateKOppfolgingstilfellePeker
                val kOppfolgingstilfellePerson = syketilfelleMock.kOppfolgingstilfellePerson

                runBlocking {
                    oppfolgingstilfelleService.receiveOppfolgingstilfelle(kOppfolgingstilfellePeker)
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

            it("should skip Oppfolgingstilfelle when no Ident is present") {
                val kOppfolgingstilfellePeker = generateKOppfolgingstilfellePekerPersonFinnesIkke

                runBlocking {
                    oppfolgingstilfelleService.receiveOppfolgingstilfelle(kOppfolgingstilfellePeker)
                }

                COUNT_OPPFOLGINGSTILFELLE_SKIPPED_FODSELSNUMMER.get() shouldBeEqualTo 1.0
            }
        }
    }
})
