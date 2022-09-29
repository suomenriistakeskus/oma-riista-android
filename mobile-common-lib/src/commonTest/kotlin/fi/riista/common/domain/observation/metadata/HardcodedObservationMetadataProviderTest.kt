package fi.riista.common.domain.observation.metadata

import fi.riista.common.domain.observation.metadata.dto.toObservationMetadata
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class HardcodedObservationMetadataProviderTest {

    @Test
    fun testMetadataExists() {
        assertNotNull(HardcodedObservationMetadataProvider.metadata)
    }

    @Test
    fun testConversionToMetadataDTOAndBack() {
        val metadataDTO = HardcodedObservationMetadataProvider.metadata.toObservationMetadataDTO()
        assertEquals(HardcodedObservationMetadataProvider.metadata, metadataDTO.toObservationMetadata())
    }
}
