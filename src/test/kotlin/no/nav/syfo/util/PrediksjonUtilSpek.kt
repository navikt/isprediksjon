package no.nav.syfo.util

import no.nav.syfo.prediksjon.PrediksjonOutput
import org.amshove.kluent.shouldBeEqualTo
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import testutil.generator.generateGenericPrediksjonOutput
import testutil.generator.generateOldPrediksjonOutput
import kotlin.test.assertFailsWith

object PrediksjonUtilSpek : Spek({

    describe("PrediksjonUtil") {
        describe("List<PrediksjonOutput>.latestPrediksjon()") {
            it("Should return latest prediksjon if only one in list") {
                val wantedPrediksjon = generateGenericPrediksjonOutput
                val prediksjonList = listOf(wantedPrediksjon)

                val actualPrediksjon = prediksjonList.latestPrediksjon()

                actualPrediksjon shouldBeEqualTo wantedPrediksjon
            }

            it("Should return latest prediksjon from a list with two Prediksjoner") {
                val wantedPrediksjon = generateGenericPrediksjonOutput
                val oldPrediksjon = generateOldPrediksjonOutput
                val prediksjonList = listOf(
                    oldPrediksjon,
                    wantedPrediksjon
                )

                val actualPrediksjon = prediksjonList.latestPrediksjon()

                actualPrediksjon shouldBeEqualTo wantedPrediksjon
            }

            it("Should throw exception if no elements in list") {
                val emptyPrediksjonList = emptyList<PrediksjonOutput>()

                assertFailsWith<NoSuchElementException> { emptyPrediksjonList.latestPrediksjon() }
            }
        }
    }
})
