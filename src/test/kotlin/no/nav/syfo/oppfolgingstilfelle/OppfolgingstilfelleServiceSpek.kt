package no.nav.syfo.oppfolgingstilfelle

import io.ktor.server.testing.*
import io.ktor.util.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import no.nav.syfo.clients.aktor.AktorService
import no.nav.syfo.clients.syketilfelle.SyketilfelleClient
import no.nav.syfo.prediksjon.PrediksjonInputService
import org.amshove.kluent.shouldBeEqualTo
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import testutil.TestDB
import testutil.UserConstants.ARBEIDSTAKER_AKTORID
import testutil.UserConstants.ARBEIDSTAKER_FNR
import testutil.dropData
import testutil.generator.generateKOppfolgingstilfelle
import testutil.generator.generateKOppfolgingstilfellePeker
import testutil.getPrediksjonInput1

@InternalAPI
object OppfolgingstilfelleServiceSpek : Spek({

    with(TestApplicationEngine()) {
        start()

        val database = TestDB()

        val aktorService = mockk<AktorService>()
        val prediksjonInputService = PrediksjonInputService(database)
        val syketilfelleClient = mockk<SyketilfelleClient>()

        val oppfolgingstilfelleService = OppfolgingstilfelleService(
            aktorService,
            prediksjonInputService,
            syketilfelleClient
        )

        val kOppfolgingstilfellePeker = generateKOppfolgingstilfellePeker
        val kOppfolgingstilfelle = generateKOppfolgingstilfelle

        afterGroup {
            database.stop()
            unmockkAll()
        }

        beforeEachTest {
            every {
                aktorService.fodselsnummerForAktor(
                    ARBEIDSTAKER_AKTORID,
                    ""
                )
            } returns ARBEIDSTAKER_FNR.value
            every {
                syketilfelleClient.getOppfolgingstilfelle(
                    ARBEIDSTAKER_AKTORID,
                    ""
                )
            } returns kOppfolgingstilfelle
        }

        afterEachTest {
            val table = "prediksjon_input"
            database.connection.dropData(table)
        }

        describe("Read and store PPrediksjonInput") {

            it("should store PrediksjonInput based on Oppfolgingstilfelle") {
                oppfolgingstilfelleService.receiveOppfolgingstilfelle(kOppfolgingstilfellePeker)

                val prediksjonInputFnrList =
                    database.connection.getPrediksjonInput1()

                prediksjonInputFnrList.size shouldBeEqualTo 1

                val returnedPrediksjonInput = prediksjonInputFnrList.first()

                returnedPrediksjonInput.fnr shouldBeEqualTo ARBEIDSTAKER_FNR.value
                returnedPrediksjonInput.aktorId shouldBeEqualTo kOppfolgingstilfellePeker.aktorId
                returnedPrediksjonInput.tilfelleStartDate shouldBeEqualTo kOppfolgingstilfelle.tidslinje.first().dag
                returnedPrediksjonInput.tilfelleEndDate shouldBeEqualTo kOppfolgingstilfelle.tidslinje.last().dag
            }
        }
    }
})
