package no.nav.syfo.prediksjon

import io.ktor.server.testing.*
import io.ktor.util.*
import io.mockk.unmockkAll
import org.amshove.kluent.shouldBeEqualTo
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import testutil.TestDB
import testutil.dropData
import testutil.generator.generatePersonOppfolgingstilfelle
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
                    val personOppfolgingstilfelle = generatePersonOppfolgingstilfelle

                    prediksjonInputService.createPrediksjonInput(personOppfolgingstilfelle)

                    val prediksjonInputFnrList =
                        database.connection.getPrediksjonInput(personOppfolgingstilfelle.fnr)

                    prediksjonInputFnrList.size shouldBeEqualTo 1

                    val returnedPrediksjonInput = prediksjonInputFnrList.first()

                    returnedPrediksjonInput.fnr shouldBeEqualTo personOppfolgingstilfelle.fnr.value
                    returnedPrediksjonInput.aktorId shouldBeEqualTo personOppfolgingstilfelle.aktorId.value
                    returnedPrediksjonInput.tilfelleStartDate shouldBeEqualTo personOppfolgingstilfelle.tilfelleStartDate
                    returnedPrediksjonInput.tilfelleEndDate shouldBeEqualTo personOppfolgingstilfelle.tilfelleEndDate
                }
            }
        }
    }
})
