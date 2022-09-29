package fi.riista.common.metadata

import fi.riista.common.domain.observation.metadata.HardcodedObservationMetadataProvider
import fi.riista.common.domain.observation.metadata.model.ObservationMetadata
import fi.riista.common.domain.srva.metadata.HardcodedSrvaMetadataProvider
import fi.riista.common.domain.srva.metadata.model.SrvaMetadata

data class MockMetadataProvider(
    override val srvaMetadata: SrvaMetadata = HardcodedSrvaMetadataProvider.metadata,
    override val observationMetadata: ObservationMetadata = HardcodedObservationMetadataProvider.metadata
): MetadataProvider {

    companion object {
        val INSTANCE = MockMetadataProvider()
    }
}