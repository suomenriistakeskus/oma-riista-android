package fi.riista.common.domain.observation.ui.common

import fi.riista.common.domain.observation.model.CommonObservationData
import fi.riista.common.domain.observation.model.ObservationSpecimenField
import fi.riista.common.domain.observation.ui.CommonObservationField
import fi.riista.common.domain.specimens.ui.SpecimenFieldDataContainer
import fi.riista.common.domain.specimens.ui.SpecimenFieldSpecification
import fi.riista.common.domain.specimens.ui.SpecimenFieldType
import fi.riista.common.domain.userInfo.CarnivoreAuthorityInformationProvider
import fi.riista.common.metadata.MetadataProvider
import fi.riista.common.resources.RR
import fi.riista.common.resources.StringProvider
import fi.riista.common.ui.dataField.FieldRequirement
import fi.riista.common.ui.dataField.FieldSpecification
import fi.riista.common.ui.dataField.SpecimenField

internal class ObservationSpecimenFieldProducer(
    private val carnivoreAuthorityInformationProvider: CarnivoreAuthorityInformationProvider,
    private val metadataProvider: MetadataProvider,
    stringProvider: StringProvider,
) {
    private val specimenFieldSpecificationsByField = mapOf(
        ObservationSpecimenField.GENDER to SpecimenFieldSpecification(
            fieldType = SpecimenFieldType.GENDER,
            label = stringProvider.getString(RR.string.gender_label),
            requirementStatus = FieldRequirement.noRequirement(),
        ),
        ObservationSpecimenField.AGE to SpecimenFieldSpecification(
            fieldType = SpecimenFieldType.AGE,
            label = stringProvider.getString(RR.string.age_label),
            requirementStatus = FieldRequirement.noRequirement(),
        ),
        ObservationSpecimenField.WIDTH_OF_PAW to SpecimenFieldSpecification(
            fieldType = SpecimenFieldType.WIDTH_OF_PAW,
            label = stringProvider.getString(RR.string.specimen_label_width_of_paw),
            requirementStatus = FieldRequirement.noRequirement(),
        ),
        ObservationSpecimenField.LENGTH_OF_PAW to SpecimenFieldSpecification(
            fieldType = SpecimenFieldType.LENGTH_OF_PAW,
            label = stringProvider.getString(RR.string.specimen_label_length_of_paw),
            requirementStatus = FieldRequirement.noRequirement(),
        ),
        ObservationSpecimenField.STATE_OF_HEALTH to SpecimenFieldSpecification(
            fieldType = SpecimenFieldType.STATE_OF_HEALTH,
            label = stringProvider.getString(RR.string.specimen_label_state_of_health),
            requirementStatus = FieldRequirement.noRequirement(),
        ),
        ObservationSpecimenField.MARKING to SpecimenFieldSpecification(
            fieldType = SpecimenFieldType.MARKING,
            label = stringProvider.getString(RR.string.specimen_label_marking),
            requirementStatus = FieldRequirement.noRequirement(),
        ),
    )

    fun createSpecimenField(
        fieldSpecification: FieldSpecification<CommonObservationField>,
        observation: CommonObservationData,
        configureSettings: (SpecimenField.DefaultSpecimenFieldSettings.() -> Unit)? = null
    ): SpecimenField<CommonObservationField> {
        val metadata = metadataProvider.observationMetadata
        val specimenFieldsInMetadata = metadata.getSpecimenFields(observation)
        val specimenFieldSpecifications = ObservationSpecimenField.values()
            .mapNotNull { specimenField ->
                specimenFieldsInMetadata[specimenField]?.toFieldRequirement(
                    isCarnivoreAuthority = carnivoreAuthorityInformationProvider.userIsCarnivoreAuthority
                )?.let {
                    // field requirement exists -> display field
                    specimenFieldSpecificationsByField[specimenField]
                }
            }
        val speciesMetadata = metadata.getSpeciesMetadata(observation)
        val contextualFields = speciesMetadata?.getContextualFields(observation)

        val specimenAmount = observation.totalSpecimenAmount
            ?: observation.specimens?.size
            ?: 1

        return SpecimenField(
            id = fieldSpecification.fieldId,
            specimenData = SpecimenFieldDataContainer(
                species = observation.species,
                specimenAmount = specimenAmount,
                specimens = observation.specimensOrEmptyList,
                fieldSpecifications = specimenFieldSpecifications,
                allowedAges = contextualFields?.allowedAges ?: listOf(),
                allowedStatesOfHealth = contextualFields?.allowedStates ?: listOf(),
                allowedMarkings = contextualFields?.allowedMarkings ?: listOf(),
                maxLengthOfPawCentimetres = speciesMetadata?.maxLengthOfPawCentimetres,
                minLengthOfPawCentimetres = speciesMetadata?.minLengthOfPawCentimetres,
                maxWidthOfPawCentimetres = speciesMetadata?.maxWidthOfPawCentimetres,
                minWidthOfPawCentimetres = speciesMetadata?.minWidthOfPawCentimetres,
            ),
            configureSettings = configureSettings
        )
    }
}