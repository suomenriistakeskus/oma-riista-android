package fi.riista.common.domain.observation.ui

import fi.riista.common.domain.model.DeerHuntingType
import fi.riista.common.domain.model.Species
import fi.riista.common.domain.observation.metadata.model.ObservationFieldRequirement
import fi.riista.common.domain.observation.model.CommonObservationData
import fi.riista.common.metadata.MetadataProvider
import fi.riista.common.ui.dataField.FieldRequirement
import fi.riista.common.ui.dataField.FieldSpecification
import fi.riista.common.ui.dataField.FieldSpecificationListBuilder
import fi.riista.common.ui.dataField.noRequirement
import fi.riista.common.ui.dataField.required
import fi.riista.common.ui.dataField.voluntary

internal class ObservationFields(
    private val metadataProvider: MetadataProvider,
) {

    /**
     * The context based on which the specifications for [CommonObservationData] fields are determined.
     */
    data class Context(
        val observation: CommonObservationData,
        val userIsCarnivoreAuthority: Boolean,
        val mode: Mode
    ) {
        enum class Mode {
            VIEW,
            EDIT,
        }
    }

    fun getFieldsToBeDisplayed(context: Context): List<FieldSpecification<CommonObservationField>> {
        return when (context.mode) {
            Context.Mode.VIEW -> getFieldsToBeDisplayedForViewing(context)
            Context.Mode.EDIT -> getFieldsToBeDisplayedForEditing(context)
        }
    }

    private fun getFieldsToBeDisplayedForViewing(context: Context): List<FieldSpecification<CommonObservationField>> {
        val observation = context.observation
        val metadataFields = metadataProvider.observationMetadata.getObservationFields(observation)

        return FieldSpecificationListBuilder<CommonObservationField>()
            .add(
                CommonObservationField.LOCATION.noRequirement(),
                CommonObservationField.SPECIES_AND_IMAGE.noRequirement(),
                CommonObservationField.DATE_AND_TIME.noRequirement(),
                CommonObservationField.OBSERVATION_CATEGORY
                    .existsInMetadataOrNull(context, metadataFields)?.noRequirement(),
                CommonObservationField.WITHIN_MOOSE_HUNTING
                    .existsInMetadataOrNull(context, metadataFields)?.noRequirement(),
                CommonObservationField.WITHIN_DEER_HUNTING
                    .existsInMetadataOrNull(context, metadataFields)?.noRequirement(),
                CommonObservationField.OBSERVATION_TYPE.noRequirement(),
                CommonObservationField.DEER_HUNTING_TYPE
                    .existsInMetadataOrNull(context, metadataFields)?.noRequirement(),
                CommonObservationField.DEER_HUNTING_OTHER_TYPE_DESCRIPTION.let { field ->
                    if (observation.deerHuntingType.value == DeerHuntingType.OTHER) {
                        field.existsInMetadataOrNull(context, metadataFields)?.noRequirement()
                    } else {
                        null
                    }
                },
                CommonObservationField.SPECIMEN_AMOUNT
                    .existsInMetadataOrNull(context, metadataFields)?.noRequirement(),
                CommonObservationField.MOOSE_LIKE_MALE_AMOUNT
                    .existsInMetadataOrNull(context, metadataFields)?.noRequirement(),
                CommonObservationField.MOOSE_LIKE_FEMALE_AMOUNT
                    .existsInMetadataOrNull(context, metadataFields)?.noRequirement(),
                CommonObservationField.MOOSE_LIKE_FEMALE_1CALF_AMOUNT
                    .existsInMetadataOrNull(context, metadataFields)?.noRequirement(),
                CommonObservationField.MOOSE_LIKE_FEMALE_2CALFS_AMOUNT
                    .existsInMetadataOrNull(context, metadataFields)?.noRequirement(),
                CommonObservationField.MOOSE_LIKE_FEMALE_3CALFS_AMOUNT
                    .existsInMetadataOrNull(context, metadataFields)?.noRequirement(),
                CommonObservationField.MOOSE_LIKE_FEMALE_4CALFS_AMOUNT
                    .existsInMetadataOrNull(context, metadataFields)?.noRequirement(),
                CommonObservationField.MOOSE_LIKE_CALF_AMOUNT
                    .existsInMetadataOrNull(context, metadataFields)?.noRequirement(),
                CommonObservationField.MOOSE_LIKE_UNKNOWN_SPECIMEN_AMOUNT
                    .existsInMetadataOrNull(context, metadataFields)?.noRequirement(),
                CommonObservationField.TASSU_VERIFIED_BY_CARNIVORE_AUTHORITY
                    .existsInMetadataOrNull(context, metadataFields)?.noRequirement(),
                CommonObservationField.TASSU_OBSERVER_NAME
                    .existsInMetadataOrNull(context, metadataFields)?.noRequirement(),
                CommonObservationField.TASSU_OBSERVER_PHONENUMBER
                    .existsInMetadataOrNull(context, metadataFields)?.noRequirement(),
                CommonObservationField.TASSU_OFFICIAL_ADDITIONAL_INFO
                    .existsInMetadataOrNull(context, metadataFields)?.noRequirement(),
                CommonObservationField.TASSU_IN_YARD_DISTANCE_TO_RESIDENCE.noRequirement()
                    .takeIf { observation.inYardDistanceToResidence != null },
                CommonObservationField.TASSU_LITTER.noRequirement().takeIf {
                    // only displayed if _contains_ a litter
                    observation.litter == true
                },
                CommonObservationField.TASSU_PACK.noRequirement().takeIf {
                    // only displayed if _contains_ a pack
                    observation.pack == true
                },
                CommonObservationField.DESCRIPTION.noRequirement(),

                // for viewing keep specimens (button) as the last item
                CommonObservationField.SPECIMENS.noRequirement().takeIf {
                    // SPECIMENS displayed only if AMOUNT is displayed
                    CommonObservationField.SPECIMEN_AMOUNT.existsInMetadataOrNull(context, metadataFields) != null
                },
            )
            .toList()
    }

    private fun getFieldsToBeDisplayedForEditing(context: Context): List<FieldSpecification<CommonObservationField>> {
        val observation = context.observation
        val metadataFields = metadataProvider.observationMetadata.getObservationFields(observation)

        val mooselikeAmountRequirementOverride: FieldRequirement? = if (context.observation.mooselikeSpecimenCount > 0) {
            FieldRequirement(FieldRequirement.Type.VOLUNTARY, indicateRequirement = true)
        } else {
            null // don't override
        }
        return FieldSpecificationListBuilder<CommonObservationField>()
            .add(
                CommonObservationField.LOCATION.required(),
                CommonObservationField.SPECIES_AND_IMAGE.required(),
                CommonObservationField.DATE_AND_TIME.required(),
                CommonObservationField.OBSERVATION_CATEGORY
                    .asFieldRequirementIfExistsInMetadata(context, metadataFields),
                CommonObservationField.WITHIN_MOOSE_HUNTING
                    .asFieldRequirementIfExistsInMetadata(context, metadataFields),
                CommonObservationField.WITHIN_DEER_HUNTING
                    .asFieldRequirementIfExistsInMetadata(context, metadataFields),
                CommonObservationField.OBSERVATION_TYPE.required()
                    .takeIf { observation.species != Species.Unknown },
                CommonObservationField.DEER_HUNTING_TYPE
                    .asFieldRequirementIfExistsInMetadata(context, metadataFields),
                CommonObservationField.DEER_HUNTING_OTHER_TYPE_DESCRIPTION.let { field ->
                    if (observation.deerHuntingType.value == DeerHuntingType.OTHER) {
                        field.asFieldRequirementIfExistsInMetadata(context, metadataFields)
                    } else {
                        null
                    }
                },
                CommonObservationField.SPECIMEN_AMOUNT
                    .asFieldRequirementIfExistsInMetadata(context, metadataFields),

                // when editing: display specimens (button) right below of amount
                CommonObservationField.SPECIMEN_AMOUNT
                    .asFieldRequirementIfExistsInMetadata(context, metadataFields)
                    ?.let { amountFieldSpecification ->
                        FieldSpecification(
                            fieldId = CommonObservationField.SPECIMENS,
                            // use same requirement status
                            requirementStatus = amountFieldSpecification.requirementStatus
                        )
                    },
                CommonObservationField.MOOSE_LIKE_MALE_AMOUNT
                    .asFieldRequirementIfExistsInMetadata(context, metadataFields, mooselikeAmountRequirementOverride),
                CommonObservationField.MOOSE_LIKE_FEMALE_AMOUNT
                    .asFieldRequirementIfExistsInMetadata(context, metadataFields, mooselikeAmountRequirementOverride),
                CommonObservationField.MOOSE_LIKE_FEMALE_1CALF_AMOUNT
                    .asFieldRequirementIfExistsInMetadata(context, metadataFields, mooselikeAmountRequirementOverride),
                CommonObservationField.MOOSE_LIKE_FEMALE_2CALFS_AMOUNT
                    .asFieldRequirementIfExistsInMetadata(context, metadataFields, mooselikeAmountRequirementOverride),
                CommonObservationField.MOOSE_LIKE_FEMALE_3CALFS_AMOUNT
                    .asFieldRequirementIfExistsInMetadata(context, metadataFields, mooselikeAmountRequirementOverride),
                CommonObservationField.MOOSE_LIKE_FEMALE_4CALFS_AMOUNT
                    .asFieldRequirementIfExistsInMetadata(context, metadataFields, mooselikeAmountRequirementOverride),
                CommonObservationField.MOOSE_LIKE_CALF_AMOUNT
                    .asFieldRequirementIfExistsInMetadata(context, metadataFields, mooselikeAmountRequirementOverride),
                CommonObservationField.MOOSE_LIKE_UNKNOWN_SPECIMEN_AMOUNT
                    .asFieldRequirementIfExistsInMetadata(context, metadataFields, mooselikeAmountRequirementOverride),
                CommonObservationField.TASSU_VERIFIED_BY_CARNIVORE_AUTHORITY
                    .asFieldRequirementIfExistsInMetadata(context, metadataFields),
                CommonObservationField.TASSU_OBSERVER_NAME
                    .asFieldRequirementIfExistsInMetadata(context, metadataFields),
                CommonObservationField.TASSU_OBSERVER_PHONENUMBER
                    .asFieldRequirementIfExistsInMetadata(context, metadataFields),
                CommonObservationField.TASSU_OFFICIAL_ADDITIONAL_INFO
                    .asFieldRequirementIfExistsInMetadata(context, metadataFields),

                CommonObservationField.TASSU_IN_YARD_DISTANCE_TO_RESIDENCE.noRequirement()
                    .takeIf { observation.inYardDistanceToResidence != null },
                CommonObservationField.TASSU_LITTER.noRequirement().takeIf {
                    // only displayed if _contains_ a litter
                    observation.litter == true
                },
                CommonObservationField.TASSU_PACK.noRequirement().takeIf {
                    // only displayed if _contains_ a pack
                    observation.pack == true
                },
                CommonObservationField.DESCRIPTION.voluntary(),
            )
            .toList()
    }

    /**
     * Return this [CommonObservationField] if field exists in given [metadataFields]
     */
    private fun CommonObservationField.existsInMetadataOrNull(
        context: Context,
        metadataFields: Map<CommonObservationField, ObservationFieldRequirement>
    ): CommonObservationField? {
        val requirementInMetadata = metadataFields[this]?.toFieldRequirement(
            isCarnivoreAuthority = context.userIsCarnivoreAuthority
        )

        return when (requirementInMetadata) {
            null -> null
            else -> this
        }
    }

    /**
     * Return this [CommonObservationField] as [FieldSpecification] if it exists in given [metadataFields]
     */
    private fun CommonObservationField.asFieldRequirementIfExistsInMetadata(
        context: Context,
        metadataFields: Map<CommonObservationField, ObservationFieldRequirement>,
        requirementOverride: FieldRequirement? = null
    ): FieldSpecification<CommonObservationField>? {
        val requirementInMetadata = metadataFields[this]?.toFieldRequirement(
            isCarnivoreAuthority = context.userIsCarnivoreAuthority
        )

        return when (requirementInMetadata) {
            null -> null
            else -> FieldSpecification(
                fieldId = this,
                requirementStatus = requirementOverride ?: requirementInMetadata
            )
        }
    }
}
