package fi.riista.common.domain.srva.metadata

import fi.riista.common.domain.srva.metadata.dto.SrvaMetadataDTO
import fi.riista.common.domain.srva.metadata.dto.toSrvaMetadata
import fi.riista.common.domain.srva.metadata.model.SrvaMetadata
import fi.riista.common.util.deserializeFromJson
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


class SrvaMetadataParsingTest {

    private val helper = SrvaMetadataTestHelperSpecVersion1

    private val metadata: SrvaMetadata
        get() = MockSrvaMetadata.METADATA_SPEC_VERSION_2.deserializeFromJson<SrvaMetadataDTO>()!!.toSrvaMetadata()

    @Test
    fun testMetadataCanBeParsedToDTO() {
        val metadataDTO: SrvaMetadataDTO? = MockSrvaMetadata.METADATA_SPEC_VERSION_2.deserializeFromJson()
        assertNotNull(metadataDTO)
    }

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
        assertEquals(4, metadata.eventCategories.count())
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

    @Test
    fun testMetadataHasFutureProofCategory() {
        val cIndex = 3 // category index

        assertEquals("SOME_FUTURE_CATEGORY", metadata.eventCategories[cIndex].categoryType.rawBackendEnumValue)

        assertEquals(2, metadata.eventCategories[cIndex].possibleEventTypes.count())
        assertEquals("TYPE_1", metadata.eventCategories[cIndex].possibleEventTypes[0].rawBackendEnumValue)
        assertEquals("TYPE_2", metadata.eventCategories[cIndex].possibleEventTypes[1].rawBackendEnumValue)

        assertEquals(2, metadata.eventCategories[cIndex].possibleEventResults.count())
        assertEquals("RESULT_1", metadata.eventCategories[cIndex].possibleEventResults[0].rawBackendEnumValue)
        assertEquals("RESULT_2", metadata.eventCategories[cIndex].possibleEventResults[1].rawBackendEnumValue)

        assertEquals(2, metadata.eventCategories[cIndex].possibleMethods.count())
        assertEquals("METHOD_1", metadata.eventCategories[cIndex].possibleMethods[0].rawBackendEnumValue)
        assertEquals("METHOD_2", metadata.eventCategories[cIndex].possibleMethods[1].rawBackendEnumValue)
    }
}
