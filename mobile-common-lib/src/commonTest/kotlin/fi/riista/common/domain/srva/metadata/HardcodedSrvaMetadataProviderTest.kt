package fi.riista.common.domain.srva.metadata

import fi.riista.common.domain.srva.metadata.model.SrvaMetadata
import kotlin.test.Test


class HardcodedSrvaMetadataProviderTest {

    private val helper = SrvaMetadataTestHelperSpecVersion1

    private val metadata: SrvaMetadata
        get() = HardcodedSrvaMetadataProvider.metadata

    @Test
    fun testMetadataHasSpecies() {
        helper.testMetadataHasSpecies(metadata)
    }

    @Test
    fun testMetadataHasAges() {
        helper.testMetadataHasAges(metadata)
    }

    @Test
    fun testMetadataHasGenders() {
        helper.testMetadataHasGenders(metadata)
    }

    @Test
    fun testMetadataHasCategories() {
        helper.testMetadataHasCategories(metadata)
    }

    @Test
    fun testMetadataHasAccidentCategory() {
        helper.testMetadataHasAccidentCategory(metadata, categoryIndex = 0)
    }

    @Test
    fun testMetadataHasDeportationCategory() {
        helper.testMetadataHasDeportationCategory(metadata, categoryIndex = 1)
    }

    @Test
    fun testMetadataHasInjuredAnimalCategory() {
        helper.testMetadataHasInjuredAnimalCategory(metadata, categoryIndex = 2)
    }

}
