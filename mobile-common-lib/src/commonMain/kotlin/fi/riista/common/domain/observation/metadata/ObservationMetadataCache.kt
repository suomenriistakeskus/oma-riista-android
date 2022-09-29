package fi.riista.common.domain.observation.metadata

import fi.riista.common.domain.constants.Constants
import fi.riista.common.domain.observation.metadata.dto.*
import fi.riista.common.domain.observation.metadata.model.ObservationMetadata
import fi.riista.common.domain.observation.metadata.model.ObservationMetadataContextualFields
import fi.riista.common.domain.observation.metadata.model.SpeciesObservationMetadata
import fi.riista.common.metadata.MetadataMemoryCache
import fi.riista.common.metadata.MetadataRepository
import fi.riista.common.metadata.MetadataSpecification
import fi.riista.common.util.deserializeFromJson
import fi.riista.common.util.serializeToJson

class ObservationMetadataCache internal constructor(
    metadataSpecification: MetadataSpecification,
    metadataRepository: MetadataRepository,
): MetadataMemoryCache<ObservationMetadata>(metadataSpecification, metadataRepository) {

    internal constructor(metadataRepository: MetadataRepository)
            : this(
        metadataSpecification = MetadataSpecification(
            metadataType = MetadataRepository.MetadataType.OBSERVATION,
            metadataSpecVersion = Constants.OBSERVATION_SPEC_VERSION.toLong(),
            metadataJsonFormatVersion = Constants.OBSERVATION_SPEC_VERSION.toLong(),
        ),
        metadataRepository = metadataRepository
    )

    override fun String.deserializeJsonToMetadata(): ObservationMetadata? {
        return deserializeFromJson<ObservationMetadataDTO>()?.toObservationMetadata()
    }

    override fun ObservationMetadata.serializeMetadataToJson(): String? {
        return toObservationMetadataDTO().serializeToJson()
    }
}


internal fun ObservationMetadata.toObservationMetadataDTO() =
    ObservationMetadataDTO(
        lastModified = lastModified,
        speciesList = speciesMetadata.map { (_, speciesMetadata) ->
            speciesMetadata.toSpeciesObservationMetadataDTO()
        },
        observationSpecVersion = observationSpecVersion,
    )

internal fun SpeciesObservationMetadata.toSpeciesObservationMetadataDTO() =
    SpeciesObservationMetadataDTO(
        speciesCode = speciesCode,
        observationFields = observationFields.toObservationFieldNameAndRequirementDTO(),
        specimenFields = specimenFields.toObservationSpecimenFieldNameAndRequirementDTO(),
        contextSensitiveFieldSets = contextSensitiveFieldSets.mapNotNull {
            it.toObservationMetadataContextualFieldsDTO()
        },
        maxLengthOfPawCentimetres = maxLengthOfPawCentimetres,
        minLengthOfPawCentimetres = minLengthOfPawCentimetres,
        maxWidthOfPawCentimetres = maxWidthOfPawCentimetres,
        minWidthOfPawCentimetres = minWidthOfPawCentimetres,
    )

private fun ObservationMetadataContextualFields.toObservationMetadataContextualFieldsDTO():
        ObservationMetadataContextualFieldsDTO? {
    val observationCategoryDTO = observationCategory.rawBackendEnumValue ?: return null
    val observationTypeDTO = observationType.rawBackendEnumValue ?: return null

    return ObservationMetadataContextualFieldsDTO(
        observationCategory = observationCategoryDTO,
        observationType = observationTypeDTO,
        observationFields = observationFields.toObservationFieldNameAndRequirementDTO(),
        specimenFields = specimenFields.toObservationSpecimenFieldNameAndRequirementDTO(),
        allowedAges = allowedAges.mapNotNull { it.rawBackendEnumValue },
        allowedStates = allowedStates.mapNotNull { it.rawBackendEnumValue },
        allowedMarkings = allowedMarkings.mapNotNull { it.rawBackendEnumValue },
    )
}