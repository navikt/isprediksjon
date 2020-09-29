package no.nav.syfo.prediksjon

import io.ktor.server.testing.*
import io.ktor.util.*
import io.mockk.unmockkAll
import no.nav.syfo.domain.Fodselsnummer
import org.amshove.kluent.shouldBeEqualTo
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import testutil.TestDB
import testutil.dropData
import testutil.generator.generatePPrediksjonInput
import testutil.getPrediksjonInput

@InternalAPI
object PrediksjonInputServiceSpek : Spek({

    val database = TestDB()

    val prediksjonInputService = PrediksjonInputService(database)

    afterGroup {
        database.stop()
        unmockkAll()
    }

    describe("PrediksjonInputService") {
        with(TestApplicationEngine()) {
            start()

            describe("Read and store PPrediksjonInput") {
                val table = "prediksjon_input"

                afterEachTest {
                    database.connection.dropData(table)
                }

                it("should store data from record") {
                    val pPrediksjonInput = generatePPrediksjonInput

                    prediksjonInputService.createPrediksjonInput(pPrediksjonInput)

                    val prediksjonInputFnrList =
                        database.connection.getPrediksjonInput(Fodselsnummer(pPrediksjonInput.fnr))

                    prediksjonInputFnrList.size shouldBeEqualTo 1

                    val returnedPrediksjonInput = prediksjonInputFnrList.first()

                    returnedPrediksjonInput.fnr shouldBeEqualTo pPrediksjonInput.fnr
                    returnedPrediksjonInput.aktorId shouldBeEqualTo pPrediksjonInput.aktorId
                    returnedPrediksjonInput.tilfelleStartDate shouldBeEqualTo pPrediksjonInput.tilfelleStartDate
                    returnedPrediksjonInput.tilfelleEndDate shouldBeEqualTo pPrediksjonInput.tilfelleEndDate
                }
            }
        }
    }
})
