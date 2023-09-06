package fi.riista.common.domain.observation.ui.modify

import fi.riista.common.domain.model.CommonLocation
import fi.riista.common.domain.model.CommonSpecimenData
import fi.riista.common.domain.model.ObservationType
import fi.riista.common.domain.model.Species
import fi.riista.common.domain.observation.metadata.model.ObservationFieldRequirement
import fi.riista.common.domain.observation.metadata.model.ObservationMetadata
import fi.riista.common.domain.observation.model.CommonObservationData
import fi.riista.common.domain.observation.model.ObservationConstants
import fi.riista.common.domain.observation.model.ObservationSpecimenField
import fi.riista.common.domain.observation.ui.CommonObservationField
import fi.riista.common.logging.getLogger
import fi.riista.common.ui.dataField.FieldSpecification

internal object CommonObservationValidator {
    enum class Error {
        MISSING_LOCATION,
        MISSING_SPECIES,
        INVALID_SPECIMEN_AMOUNT,
        SPECIMEN_AMOUNT_AT_LEAST_TWO,
        MISSING_DATE_AND_TIME,
        MISSING_OBSERVATION_TYPE,
        MISSING_OBSERVATION_CATEGORY,
        MISSING_WITHIN_MOOSE_HUNTING,
        MISSING_WITHIN_DEER_HUNTING,
        MISSING_DEER_HUNTING_TYPE,
        MISSING_DEER_HUNTING_OTHER_TYPE_DESCRIPTION,
        MISSING_SPECIMENS,
        INVALID_SPECIMENS,
        MISSING_MOOSE_LIKE_MALE_AMOUNT,
        MISSING_MOOSE_LIKE_FEMALE_AMOUNT,
        MISSING_MOOSE_LIKE_FEMALE_1CALF_AMOUNT,
        MISSING_MOOSE_LIKE_FEMALE_2CALFS_AMOUNT,
        MISSING_MOOSE_LIKE_FEMALE_3CALFS_AMOUNT,
        MISSING_MOOSE_LIKE_FEMALE_4CALFS_AMOUNT,
        MISSING_MOOSE_LIKE_CALF_AMOUNT,
        MISSING_MOOSE_LIKE_UNKNOWN_SPECIMEN_AMOUNT,
        MISSING_TASSU_VERIFIED_BY_CARNIVORE_AUTHORITY,
        MISSING_TASSU_OBSERVER_NAME,
        MISSING_TASSU_OBSERVER_PHONENUMBER,
        MISSING_TASSU_OFFICIAL_ADDITIONAL_INFO,
        MISSING_TASSU_IN_YARD_DISTANCE_TO_RESIDENCE,
        MISSING_TASSU_LITTER,
        MISSING_TASSU_PACK,
        MISSING_DESCRIPTION,
    }

    private val logger by getLogger(CommonObservationValidator::class)

    fun validate(
        observation: CommonObservationData,
        observationMetadata: ObservationMetadata,
        displayedFields: List<FieldSpecification<CommonObservationField>>,
    ): List<Error> {
        return displayedFields.mapNotNull { fieldSpecification ->
            when (fieldSpecification.fieldId) {
                CommonObservationField.LOCATION -> {
                    fieldSpecification.ifRequired {
                        Error.MISSING_LOCATION.takeIf {
                            observation.location is CommonLocation.Unknown
                        }
                    }
                }
                CommonObservationField.SPECIES_AND_IMAGE ->
                    fieldSpecification.ifRequired {
                        Error.MISSING_SPECIES.takeIf {
                            observation.species is Species.Unknown
                        }
                    }
                CommonObservationField.DATE_AND_TIME ->
                        fieldSpecification.ifRequired {
                            Error.MISSING_DATE_AND_TIME.takeIf {
                                // field is non-nullable currently but let's keep validation anyway in case
                                // field ever becomes nullable
                                @Suppress("SENSELESS_COMPARISON")
                                observation.pointOfTime == null
                            }
                        }
                CommonObservationField.OBSERVATION_TYPE ->
                    fieldSpecification.ifRequired {
                        Error.MISSING_OBSERVATION_TYPE.takeIf {
                            observation.observationType.rawBackendEnumValue == null
                        }
                    }
                CommonObservationField.OBSERVATION_CATEGORY ->
                    fieldSpecification.ifRequired {
                        Error.MISSING_OBSERVATION_CATEGORY.takeIf {
                            observation.observationCategory.rawBackendEnumValue == null
                        }
                    }
                CommonObservationField.WITHIN_MOOSE_HUNTING ->
                    fieldSpecification.ifRequired {
                        Error.MISSING_WITHIN_MOOSE_HUNTING.takeIf {
                            // value contained within observationCategory
                            observation.observationCategory.rawBackendEnumValue == null
                        }
                    }
                CommonObservationField.WITHIN_DEER_HUNTING ->
                    fieldSpecification.ifRequired {
                        Error.MISSING_WITHIN_DEER_HUNTING.takeIf {
                            // value contained within observationCategory
                            observation.observationCategory.rawBackendEnumValue == null
                        }
                    }
                CommonObservationField.DEER_HUNTING_TYPE ->
                    fieldSpecification.ifRequired {
                        Error.MISSING_DEER_HUNTING_TYPE.takeIf {
                            observation.deerHuntingType.rawBackendEnumValue == null
                        }
                    }
                CommonObservationField.DEER_HUNTING_OTHER_TYPE_DESCRIPTION ->
                    fieldSpecification.ifRequired {
                        Error.MISSING_DEER_HUNTING_OTHER_TYPE_DESCRIPTION.takeIf {
                            observation.deerHuntingOtherTypeDescription == null
                        }
                    }
                CommonObservationField.SPECIMEN_AMOUNT ->
                    fieldSpecification.ifRequired {
                        val specimenAmount = observation.totalSpecimenAmount ?: 0
                        listOfNotNull(
                            Error.SPECIMEN_AMOUNT_AT_LEAST_TWO.takeIf {
                                observation.observationType.value == ObservationType.POIKUE && specimenAmount < 2
                            },
                            Error.INVALID_SPECIMEN_AMOUNT.takeIf {
                                specimenAmount == 0 || specimenAmount > ObservationConstants.MAX_SPECIMEN_AMOUNT
                            }
                        ).firstOrNull()
                    }
                CommonObservationField.SPECIMENS -> {
                    val specimens = observation.specimensOrEmptyList
                    val specimenFieldRequired = observationMetadata.getSpecimenFields(observation).any {
                        it.value == ObservationFieldRequirement.YES
                    }
                    if (specimenFieldRequired && specimens.isEmpty()) {
                        return@mapNotNull Error.MISSING_SPECIMENS
                    }

                    Error.INVALID_SPECIMENS.takeIf {
                        // validate only first N specimens where N is determined by the amount set by user
                        // - it is possible that there are more than N specimens in the list and rest may
                        //   contain invalid data (they are allowed to)
                        val specimenAmount = observation.totalSpecimenAmount ?: 0
                        specimens.take(specimenAmount).any { specimenData ->
                            anyMissingSpecimenFields(
                                observation = observation,
                                specimen = specimenData,
                                observationMetadata = observationMetadata
                            )
                        }
                    }
                }
                CommonObservationField.MOOSE_LIKE_MALE_AMOUNT ->
                    fieldSpecification.ifRequired {
                        Error.MISSING_MOOSE_LIKE_MALE_AMOUNT.takeIf {
                            observation.mooselikeMaleAmount == null || observation.mooselikeSpecimenCount == 0
                        }
                    }
                CommonObservationField.MOOSE_LIKE_FEMALE_AMOUNT ->
                    fieldSpecification.ifRequired {
                        Error.MISSING_MOOSE_LIKE_FEMALE_AMOUNT.takeIf {
                            observation.mooselikeFemaleAmount == null || observation.mooselikeSpecimenCount == 0
                        }
                    }
                CommonObservationField.MOOSE_LIKE_FEMALE_1CALF_AMOUNT ->
                    fieldSpecification.ifRequired {
                        Error.MISSING_MOOSE_LIKE_FEMALE_1CALF_AMOUNT.takeIf {
                            observation.mooselikeFemale1CalfAmount == null || observation.mooselikeSpecimenCount == 0
                        }
                    }
                CommonObservationField.MOOSE_LIKE_FEMALE_2CALFS_AMOUNT ->
                    fieldSpecification.ifRequired {
                        Error.MISSING_MOOSE_LIKE_FEMALE_2CALFS_AMOUNT.takeIf {
                            observation.mooselikeFemale2CalfsAmount == null || observation.mooselikeSpecimenCount == 0
                        }
                    }
                CommonObservationField.MOOSE_LIKE_FEMALE_3CALFS_AMOUNT ->
                    fieldSpecification.ifRequired {
                        Error.MISSING_MOOSE_LIKE_FEMALE_3CALFS_AMOUNT.takeIf {
                            observation.mooselikeFemale3CalfsAmount == null || observation.mooselikeSpecimenCount == 0
                        }
                    }
                CommonObservationField.MOOSE_LIKE_FEMALE_4CALFS_AMOUNT ->
                    fieldSpecification.ifRequired {
                        Error.MISSING_MOOSE_LIKE_FEMALE_4CALFS_AMOUNT.takeIf {
                            observation.mooselikeFemale4CalfsAmount == null || observation.mooselikeSpecimenCount == 0
                        }
                    }
                CommonObservationField.MOOSE_LIKE_CALF_AMOUNT ->
                    fieldSpecification.ifRequired {
                        Error.MISSING_MOOSE_LIKE_CALF_AMOUNT.takeIf {
                            observation.mooselikeCalfAmount == null || observation.mooselikeSpecimenCount == 0
                        }
                    }
                CommonObservationField.MOOSE_LIKE_UNKNOWN_SPECIMEN_AMOUNT ->
                    fieldSpecification.ifRequired {
                        Error.MISSING_MOOSE_LIKE_UNKNOWN_SPECIMEN_AMOUNT.takeIf {
                            observation.mooselikeUnknownSpecimenAmount == null || observation.mooselikeSpecimenCount == 0
                        }
                    }
                CommonObservationField.TASSU_VERIFIED_BY_CARNIVORE_AUTHORITY ->
                    fieldSpecification.ifRequired {
                        Error.MISSING_TASSU_VERIFIED_BY_CARNIVORE_AUTHORITY.takeIf {
                            observation.verifiedByCarnivoreAuthority == null
                        }
                    }
                CommonObservationField.TASSU_OBSERVER_NAME ->
                    fieldSpecification.ifRequired {
                        Error.MISSING_TASSU_OBSERVER_NAME.takeIf {
                            observation.observerName == null
                        }
                    }
                CommonObservationField.TASSU_OBSERVER_PHONENUMBER ->
                    fieldSpecification.ifRequired {
                        Error.MISSING_TASSU_OBSERVER_PHONENUMBER.takeIf {
                            observation.observerPhoneNumber == null
                        }
                    }
                CommonObservationField.TASSU_OFFICIAL_ADDITIONAL_INFO ->
                    fieldSpecification.ifRequired {
                        Error.MISSING_TASSU_OFFICIAL_ADDITIONAL_INFO.takeIf {
                            observation.officialAdditionalInfo == null
                        }
                    }
                CommonObservationField.TASSU_IN_YARD_DISTANCE_TO_RESIDENCE ->
                    fieldSpecification.ifRequired {
                        Error.MISSING_TASSU_IN_YARD_DISTANCE_TO_RESIDENCE.takeIf {
                            // should not happen as distance to residence should be readonly field
                            observation.inYardDistanceToResidence == null
                        }
                    }
                CommonObservationField.TASSU_LITTER ->
                    fieldSpecification.ifRequired {
                        Error.MISSING_TASSU_LITTER.takeIf {
                            // should not happen as litter should be readonly field
                            observation.litter == null
                        }
                    }
                CommonObservationField.TASSU_PACK ->
                    fieldSpecification.ifRequired {
                        Error.MISSING_TASSU_PACK.takeIf {
                            // should not happen as observer phone number should be readonly field
                            observation.pack == null
                        }
                    }
                CommonObservationField.DESCRIPTION ->
                    fieldSpecification.ifRequired {
                        Error.MISSING_DESCRIPTION.takeIf {
                            observation.description == null
                        }
                    }
                CommonObservationField.ERROR_SPECIMEN_AMOUNT_AT_LEAST_TWO -> null
            }
        }.also { errors ->
            if (errors.isEmpty()) {
                logger.v { "Observation is valid!" }
            } else {
                logger.d { "Observation validation errors: $errors" }
            }
        }
    }

    private fun anyMissingSpecimenFields(
        observation: CommonObservationData,
        specimen: CommonSpecimenData,
        observationMetadata: ObservationMetadata,
    ): Boolean {
        val specimenFields = observationMetadata.getSpecimenFields(observation)

        return specimenFields.any { (field, fieldRequirement) ->
            if (fieldRequirement == ObservationFieldRequirement.YES) {
                specimen.isFieldMissing(field)
            } else {
                false
            }
        }
    }

    private fun CommonSpecimenData.isFieldMissing(field: ObservationSpecimenField): Boolean {
        return when (field) {
            ObservationSpecimenField.GENDER -> gender == null // unknown gender is allowed
            ObservationSpecimenField.AGE -> age == null // unknown age is allowed
            ObservationSpecimenField.STATE_OF_HEALTH -> stateOfHealth == null
            ObservationSpecimenField.MARKING -> marking == null
            ObservationSpecimenField.WIDTH_OF_PAW -> widthOfPaw == null // 0.0 currently allowed
            ObservationSpecimenField.LENGTH_OF_PAW -> lengthOfPaw == null // 0.0 currently allowed
        }
    }
}
