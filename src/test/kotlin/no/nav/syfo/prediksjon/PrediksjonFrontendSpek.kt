package no.nav.syfo.prediksjon

import org.amshove.kluent.shouldBeEqualTo
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import testutil.generator.generatePrediksjonOutputLong
import testutil.generator.generatePrediksjonOutputShort

object PrediksjonFrontendSpek : Spek({

    describe("PrediksjonFrontend") {
        describe("toPrediksjonFrontend should convert PrediksjonOutput to PrediksjonFrontend") {
            it("Should give object with langt == true if delta is more than 28 weeks") {
                val prediksjonOutput = generatePrediksjonOutputLong

                val prediksjonFrontend = prediksjonOutput.toPrediksjonFrontend()

                prediksjonFrontend.langt shouldBeEqualTo true
                prediksjonFrontend.lengreVarighetGrunner shouldBeEqualTo prediksjonOutput.forklaring.opp
                prediksjonFrontend.kortereVarighetGrunner shouldBeEqualTo prediksjonOutput.forklaring.ned
                prediksjonFrontend.prediksjonsDato shouldBeEqualTo prediksjonOutput.prediksjonCreated
            }

            it("Should give object with langt == false if delta is less than 28 weeks") {
                val prediksjonOutput = generatePrediksjonOutputShort

                val prediksjonFrontend = prediksjonOutput.toPrediksjonFrontend()

                prediksjonFrontend.langt shouldBeEqualTo false
                prediksjonFrontend.lengreVarighetGrunner shouldBeEqualTo prediksjonOutput.forklaring.opp
                prediksjonFrontend.kortereVarighetGrunner shouldBeEqualTo prediksjonOutput.forklaring.ned
                prediksjonFrontend.prediksjonsDato shouldBeEqualTo prediksjonOutput.prediksjonCreated
            }
        }
    }
})
