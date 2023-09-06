package fi.riista.common.ui.helpers

import fi.riista.common.constants.TestConstants
import fi.riista.common.domain.constants.SpeciesCodes
import fi.riista.common.domain.model.Species
import fi.riista.common.helpers.TestStringProvider
import kotlin.test.Test
import kotlin.test.assertEquals

class WeightFormatterTest {

    private val weightFormatter = WeightFormatter(stringProvider = TestStringProvider.INSTANCE)

    @Test
    fun testDecimalsForDeer() {
        SpeciesCodes.DEER_ANIMALS.forEach { speciesCode ->
            assertEquals(0, WeightFormatter.getDecimalCount(species = Species.Known(speciesCode)))
        }
    }

    @Test
    fun testDecimalsForMoose() {
        assertEquals(0, WeightFormatter.getDecimalCount(species = Species.Known(SpeciesCodes.MOOSE_ID)))
    }

    @Test
    fun testDecimalsForOtherSpecies() {
        @Suppress("ConvertArgumentToSet")
        (TestConstants.ALL_SPECIES - SpeciesCodes.DEER_ANIMALS - SpeciesCodes.MOOSE_ID).forEach { speciesCode ->
            assertEquals(1, WeightFormatter.getDecimalCount(species = Species.Known(speciesCode)))
        }
    }

    @Test
    fun testOneDecimalWeightFormatting() {
        // change to different species if Bear loses decimals
        val species = Species.Known(SpeciesCodes.BEAR_ID)

        assertEquals("0.0", getFormattedWeight(0.0, species))
        assertEquals("1.0", getFormattedWeight(1.0, species))
        assertEquals("1.1", getFormattedWeight(1.1, species))
        assertEquals("20.1", getFormattedWeight(20.1, species), "20.1")
        assertEquals("20.1", getFormattedWeight(20.12, species), "20.12")
        assertEquals("20.1", getFormattedWeight(20.18, species), "20.18") // not-rounded
        assertEquals("20.1", getFormattedWeight(20.182, species), "20.182") // not-rounded
    }

    private fun getFormattedWeight(weight: Double, species: Species) = weightFormatter.formatWeight(weight, species)
}
