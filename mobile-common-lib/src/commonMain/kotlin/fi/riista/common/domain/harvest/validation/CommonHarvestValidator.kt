package fi.riista.common.domain.harvest.validation

import fi.riista.common.domain.content.SpeciesResolver
import fi.riista.common.domain.harvest.model.CommonHarvestData
import fi.riista.common.domain.harvest.model.HarvestConstants
import fi.riista.common.domain.harvest.model.HarvestReportingType
import fi.riista.common.domain.harvest.ui.CommonHarvestField
import fi.riista.common.domain.harvest.ui.common.HarvestSpecimenFieldProducer
import fi.riista.common.domain.harvest.ui.fields.HarvestSpecimenFieldRequirementResolver
import fi.riista.common.domain.model.CommonLocation
import fi.riista.common.domain.model.CommonSpecimenData
import fi.riista.common.domain.model.GameAge
import fi.riista.common.domain.model.Gender
import fi.riista.common.domain.model.SearchableOrganization
import fi.riista.common.domain.model.Species
import fi.riista.common.domain.permit.harvestPermit.CommonHarvestPermit
import fi.riista.common.domain.specimens.ui.SpecimenFieldType
import fi.riista.common.logging.getLogger
import fi.riista.common.model.isWithinPeriods
import fi.riista.common.ui.dataField.FieldRequirement
import fi.riista.common.ui.dataField.FieldSpecification
import fi.riista.common.util.LocalDateTimeProvider
import fi.riista.common.util.isNullOr


class CommonHarvestValidator(
    private val localDateTimeProvider: LocalDateTimeProvider,
    private val speciesResolver: SpeciesResolver,
) {
    enum class Error {
        MISSING_LOCATION,
        MISSING_PERMIT_INFORMATION,
        MISSING_SPECIES,
        INVALID_SPECIES,
        SPECIES_NOT_WITHIN_PERMIT,
        INVALID_SPECIMEN_AMOUNT,
        MISSING_SPECIMENS,
        INVALID_SPECIMENS,
        MISSING_DATE_AND_TIME,
        DATE_NOT_WITHIN_PERMIT, // either group hunting permit or common permit depending on context
        DATETIME_IN_FUTURE,
        MISSING_HUNTING_DAY,
        TIME_NOT_WITHIN_HUNTING_DAY,
        MISSING_DEER_HUNTING_TYPE,
        MISSING_DEER_HUNTING_OTHER_TYPE_DESCRIPTION,
        MISSING_ACTOR,
        MISSING_SELECTED_CLUB,
        INVALID_SELECTED_CLUB_OFFICIAL_CODE,
        MISSING_GENDER,
        INVALID_GENDER,
        MISSING_AGE,
        INVALID_AGE,
        MISSING_ALONE,
        MISSING_NOT_EDIBLE,
        MISSING_WEIGHT_ESTIMATED,
        MISSING_WEIGHT_MEASURED,
        MISSING_FITNESS_CLASS,
        MISSING_ANTLERS_LOST,
        MISSING_ANTLERS_TYPE,
        MISSING_ANTLERS_WIDTH,
        MISSING_ANTLER_POINTS_LEFT,
        MISSING_ANTLER_POINTS_RIGHT,
        MISSING_ANTLERS_GIRTH,
        MISSING_ANTLER_SHAFT_WIDTH,
        MISSING_ANTLERS_LENGTH,
        MISSING_ANTLERS_INNER_WIDTH,
        MISSING_ADDITIONAL_INFORMATION,
        MISSING_DESCRIPTION,
        MISSING_WEIGHT,
        MISSING_WILD_BOAR_FEEDING_PLACE,
        MISSING_GREY_SEAL_HUNTING_METHOD,
        MISSING_TAIGA_BEAN_GOOSE,
    }

    private val logger by getLogger(CommonHarvestValidator::class)

    internal fun validate(
        harvest: CommonHarvestData,
        permit: CommonHarvestPermit?,
        harvestReportingType: HarvestReportingType,
        displayedFields: List<FieldSpecification<CommonHarvestField>>,
    ): List<Error> {
        val speciesCode = harvest.species.knownSpeciesCodeOrNull()
        val multipleSpecimensAllowed = speciesCode?.let {
            speciesResolver.getMultipleSpecimensAllowedOnHarvests(speciesCode = it)
        } ?: false

        val specimenFieldRequirements = speciesCode
            ?.let {
                HarvestSpecimenFieldProducer.getSpecimenFieldTypes(speciesCode = it)
            }?.associateWith { specimenFieldType ->
                HarvestSpecimenFieldRequirementResolver.resolveRequirementType(
                    specimenField = specimenFieldType,
                    speciesCode = speciesCode,
                    harvestReportingType = harvestReportingType,
                )
            }
        val specimenFieldsRequired = specimenFieldRequirements?.any {
            it.value == FieldRequirement.Type.REQUIRED
        } ?: false

        // for harvests, at least one specimen is required if at least one specimen field is required.
        // Required specimen field can either be displayed in a separate view (A) or it may be displayed among
        // other harvest data (B)
        //
        // Note: specimen is not required to exist even though only one specimen is allowed. This would be
        //       the case for certain species which only have voluntary specimen fields (e.g. villiintynyt kissa)

        // Check for the case (A) where specimen field is (most likely) displayed in a separate view
        var missingSpecimenError: Error? = if (specimenFieldsRequired && harvest.specimens.isEmpty()) {
            Error.MISSING_SPECIMENS
        } else {
            null
        }

        // check the case (B) by noticing whether specimen information is required when fields are being validated
        val accessSpecimen: (FieldSpecification<CommonHarvestField>) -> CommonSpecimenData? = { fieldSpecification ->
            val specimen = harvest.specimens.getOrNull(0)
            if (specimen == null && fieldSpecification.isRequired()) {
                // specimen was required but missing
                missingSpecimenError = Error.MISSING_SPECIMENS
            }
            specimen
        }

        val fieldErrors = displayedFields.mapNotNull { fieldSpecification ->
            when (fieldSpecification.fieldId) {
                CommonHarvestField.LOCATION -> {
                    fieldSpecification.ifRequired {
                        Error.MISSING_LOCATION.takeIf {
                            harvest.location is CommonLocation.Unknown
                        }
                    }
                }
                // either SELECT_PERMIT or PERMIT_INFORMATION would be ok here
                CommonHarvestField.SELECT_PERMIT ->
                    fieldSpecification.ifRequired {
                        Error.MISSING_PERMIT_INFORMATION.takeIf {
                            harvest.permitNumber == null
                        }
                    }
                CommonHarvestField.SPECIES_CODE,
                CommonHarvestField.SPECIES_CODE_AND_IMAGE ->
                    when (harvest.species) {
                        is Species.Known -> Error.SPECIES_NOT_WITHIN_PERMIT.takeIf {
                            permit != null && permit.isAvailableForSpecies(harvest.species).not()
                        }
                        Species.Other -> Error.INVALID_SPECIES // other species not allowed for harvests
                        Species.Unknown -> Error.MISSING_SPECIES.takeIf { fieldSpecification.isRequired() }
                    }
                CommonHarvestField.DATE_AND_TIME -> {
                    fieldSpecification.ifRequired {
                        @Suppress("SENSELESS_COMPARISON")
                        when {
                            // field is non-nullable currently but let's keep validation anyway in case
                            // field ever becomes nullable
                            harvest.pointOfTime == null -> Error.MISSING_DATE_AND_TIME
                            permit != null && !harvest.isWithinPermitValidityPeriods(permit) -> Error.DATE_NOT_WITHIN_PERMIT
                            harvest.pointOfTime > localDateTimeProvider.now() -> Error.DATETIME_IN_FUTURE
                            else -> null
                        }
                    }
                }
                CommonHarvestField.SPECIMEN_AMOUNT -> {
                    val specimenAmount = harvest.amount ?: 0
                    Error.INVALID_SPECIMEN_AMOUNT.takeIf {
                        specimenAmount == 0 ||
                                (!multipleSpecimensAllowed && specimenAmount > 1) ||
                                (multipleSpecimensAllowed && specimenAmount > HarvestConstants.MAX_SPECIMEN_AMOUNT)
                    }
                }
                CommonHarvestField.SPECIMENS -> {
                    if (specimenFieldsRequired && harvest.specimens.isEmpty()) {
                        logger.w {
                            "Specimen fields required but there are no specimens!"
                        }
                        return@mapNotNull Error.MISSING_SPECIMENS
                    }

                    Error.INVALID_SPECIMENS.takeIf {
                        // only validate specimens if multiple specimens are allowed. If only
                        // one specimen is allowed then:
                        // - this field should not be displayed at all!
                        // - specimen fields are displayed among other harvest fields
                        //
                        // prevent specimen validation if only one specimen is allowed since
                        // the specimen fields may have requirements that are not indicated in harvest fields
                        if (!multipleSpecimensAllowed) {
                            logger.w {
                                "Refusing to produce INVALID_SPECIMENS error as only one harvest specimen is allowed"
                            }
                        }
                        multipleSpecimensAllowed
                    }?.takeIf {
                        if (specimenFieldRequirements == null) {
                            return@takeIf false
                        }

                        // validate only first N specimens where N is determined by the amount set by user
                        // - it is possible that there are more than N specimens in the list and rest may
                        //   contain invalid data (they are allowed to)
                        val specimenAmount = harvest.amount ?: 0
                        harvest.specimens.take(specimenAmount).any { specimenData ->
                            anyMissingOrInvalidSpecimenFields(
                                specimen = specimenData,
                                specimenFieldRequirements = specimenFieldRequirements,
                            )
                        }
                    }
                }
                CommonHarvestField.DEER_HUNTING_TYPE -> {
                    fieldSpecification.ifRequired {
                        Error.MISSING_DEER_HUNTING_TYPE.takeIf {
                            harvest.deerHuntingType.rawBackendEnumValue == null
                        }
                    }
                }
                CommonHarvestField.DEER_HUNTING_OTHER_TYPE_DESCRIPTION -> {
                    fieldSpecification.ifRequired {
                        Error.MISSING_DEER_HUNTING_OTHER_TYPE_DESCRIPTION.takeIf {
                            harvest.deerHuntingOtherTypeDescription == null
                        }
                    }
                }
                CommonHarvestField.ACTOR -> {
                    fieldSpecification.ifRequired {
                        Error.MISSING_ACTOR.takeIf {
                            harvest.actorInfo.personWithHunterNumber == null
                        }
                    }
                }
                CommonHarvestField.SELECTED_CLUB -> {
                    Error.MISSING_SELECTED_CLUB.takeIf {
                        fieldSpecification.isRequired() && harvest.selectedClub.exists().not()
                    }
                }
                CommonHarvestField.SELECTED_CLUB_OFFICIAL_CODE -> {
                    Error.INVALID_SELECTED_CLUB_OFFICIAL_CODE.takeIf {
                        // don't allow saving while searching
                        harvest.selectedClub is SearchableOrganization.Searching
                    }
                }
                CommonHarvestField.GENDER -> {
                    val gender = accessSpecimen(fieldSpecification)?.gender?.value
                    when {
                        gender == null -> fieldSpecification.ifRequired {
                            Error.MISSING_GENDER
                        }
                        gender == Gender.UNKNOWN && !harvest.unknownGenderAllowed -> Error.INVALID_GENDER
                        else -> null
                    }
                }
                CommonHarvestField.AGE -> {
                    val age = accessSpecimen(fieldSpecification)?.age?.value
                    when {
                        age == null -> fieldSpecification.ifRequired {
                            Error.MISSING_AGE
                        }
                        age == GameAge.UNKNOWN && !harvest.unknownAgeAllowed -> Error.INVALID_AGE
                        else -> null
                    }
                }
                CommonHarvestField.ALONE -> {
                    fieldSpecification.ifRequired {
                        Error.MISSING_ALONE.takeIf {
                            accessSpecimen(fieldSpecification)?.alone == null
                        }
                    }
                }
                CommonHarvestField.NOT_EDIBLE -> {
                    fieldSpecification.ifRequired {
                        Error.MISSING_NOT_EDIBLE.takeIf {
                            accessSpecimen(fieldSpecification)?.notEdible == null
                        }
                    }
                }
                CommonHarvestField.WEIGHT_ESTIMATED -> {
                    fieldSpecification.ifRequired {
                        Error.MISSING_WEIGHT_ESTIMATED.takeIf {
                            accessSpecimen(fieldSpecification)?.weightEstimated == null
                        }
                    }
                }
                CommonHarvestField.WEIGHT_MEASURED -> {
                    fieldSpecification.ifRequired {
                        Error.MISSING_WEIGHT_MEASURED.takeIf {
                            accessSpecimen(fieldSpecification)?.weightMeasured == null
                        }
                    }
                }
                CommonHarvestField.FITNESS_CLASS -> {
                    fieldSpecification.ifRequired {
                        Error.MISSING_FITNESS_CLASS.takeIf {
                            accessSpecimen(fieldSpecification)?.fitnessClass?.rawBackendEnumValue == null
                        }
                    }

                }
                CommonHarvestField.ANTLERS_LOST -> {
                    fieldSpecification.ifRequired {
                        Error.MISSING_ANTLERS_LOST.takeIf {
                            accessSpecimen(fieldSpecification)?.antlersLost == null
                        }
                    }
                }
                CommonHarvestField.ANTLERS_TYPE -> {
                    fieldSpecification.ifRequired {
                        Error.MISSING_ANTLERS_TYPE.takeIf {
                            accessSpecimen(fieldSpecification)?.antlersType?.rawBackendEnumValue == null
                        }
                    }
                }
                CommonHarvestField.ANTLERS_WIDTH -> {
                    fieldSpecification.ifRequired {
                        Error.MISSING_ANTLERS_WIDTH.takeIf {
                            accessSpecimen(fieldSpecification)?.antlersWidth == null
                        }
                    }
                }
                CommonHarvestField.ANTLER_POINTS_LEFT -> {
                    fieldSpecification.ifRequired {
                        Error.MISSING_ANTLER_POINTS_LEFT.takeIf {
                            accessSpecimen(fieldSpecification)?.antlerPointsLeft == null
                        }
                    }
                }
                CommonHarvestField.ANTLER_POINTS_RIGHT -> {
                    fieldSpecification.ifRequired {
                        Error.MISSING_ANTLER_POINTS_RIGHT.takeIf {
                            accessSpecimen(fieldSpecification)?.antlerPointsRight == null
                        }
                    }
                }
                CommonHarvestField.ANTLERS_GIRTH -> {
                    fieldSpecification.ifRequired {
                        Error.MISSING_ANTLERS_GIRTH.takeIf {
                            accessSpecimen(fieldSpecification)?.antlersGirth == null
                        }
                    }
                }
                CommonHarvestField.ANTLER_SHAFT_WIDTH -> {
                    fieldSpecification.ifRequired {
                        Error.MISSING_ANTLER_SHAFT_WIDTH.takeIf {
                            accessSpecimen(fieldSpecification)?.antlerShaftWidth == null
                        }
                    }
                }
                CommonHarvestField.ANTLERS_LENGTH -> {
                    fieldSpecification.ifRequired {
                        Error.MISSING_ANTLERS_LENGTH.takeIf {
                            accessSpecimen(fieldSpecification)?.antlersLength == null
                        }
                    }
                }
                CommonHarvestField.ANTLERS_INNER_WIDTH -> {
                    fieldSpecification.ifRequired {
                        Error.MISSING_ANTLERS_INNER_WIDTH.takeIf {
                            accessSpecimen(fieldSpecification)?.antlersInnerWidth == null
                        }
                    }
                }
                CommonHarvestField.ADDITIONAL_INFORMATION -> {
                    fieldSpecification.ifRequired {
                        Error.MISSING_ADDITIONAL_INFORMATION.takeIf {
                            accessSpecimen(fieldSpecification)?.additionalInfo == null
                        }
                    }
                }
                CommonHarvestField.DESCRIPTION ->
                    fieldSpecification.ifRequired {
                        Error.MISSING_DESCRIPTION.takeIf {
                            harvest.description == null
                        }
                    }
                CommonHarvestField.WEIGHT ->
                    fieldSpecification.ifRequired {
                        Error.MISSING_WEIGHT.takeIf {
                            accessSpecimen(fieldSpecification)?.weight == null
                        }
                    }
                CommonHarvestField.WILD_BOAR_FEEDING_PLACE ->
                    fieldSpecification.ifRequired {
                        Error.MISSING_WILD_BOAR_FEEDING_PLACE.takeIf {
                            harvest.feedingPlace == null
                        }
                    }
                CommonHarvestField.GREY_SEAL_HUNTING_METHOD ->
                    fieldSpecification.ifRequired {
                        Error.MISSING_GREY_SEAL_HUNTING_METHOD.takeIf {
                            harvest.greySealHuntingMethod.rawBackendEnumValue == null
                        }
                    }
                CommonHarvestField.IS_TAIGA_BEAN_GOOSE ->
                    fieldSpecification.ifRequired {
                        Error.MISSING_TAIGA_BEAN_GOOSE.takeIf {
                            harvest.taigaBeanGoose == null
                        }
                    }

                // explicitly add the ones which don't need any validation
                CommonHarvestField.HARVEST_REPORT_STATE,
                CommonHarvestField.OWN_HARVEST,
                CommonHarvestField.ACTOR_HUNTER_NUMBER,
                CommonHarvestField.ACTOR_HUNTER_NUMBER_INFO_OR_ERROR,
                CommonHarvestField.SELECTED_CLUB_OFFICIAL_CODE_INFO_OR_ERROR,
                CommonHarvestField.ANTLER_INSTRUCTIONS,
                CommonHarvestField.ADDITIONAL_INFORMATION_INSTRUCTIONS,
                CommonHarvestField.ERROR_DATE_NOT_WITHIN_PERMIT,
                CommonHarvestField.ERROR_DATETIME_IN_FUTURE,
                CommonHarvestField.ERROR_TIME_NOT_WITHIN_HUNTING_DAY,
                CommonHarvestField.HUNTING_DAY_AND_TIME,
                CommonHarvestField.HEADLINE_SHOOTER,
                CommonHarvestField.AUTHOR,
                CommonHarvestField.HEADLINE_SPECIMEN,
                CommonHarvestField.PERMIT_INFORMATION,
                CommonHarvestField.PERMIT_REQUIRED_NOTIFICATION -> {
                    null
                }
            }
        }

        return (listOfNotNull(missingSpecimenError) + fieldErrors).also { errors ->
            if (errors.isEmpty()) {
                logger.v { "Harvest is valid!" }
            } else {
                logger.v { "Harvest validation errors: $errors" }
            }
        }
    }

    private fun CommonHarvestData.isWithinPermitValidityPeriods(permit: CommonHarvestPermit): Boolean {
        // no species amounts --> harvest cannot be within permit validity periods
        val speciesAmounts = permit.getSpeciesAmountFor(species) ?: return false

        return pointOfTime.date.isWithinPeriods(speciesAmounts.validityPeriods)
    }

    private fun anyMissingOrInvalidSpecimenFields(
        specimen: CommonSpecimenData,
        specimenFieldRequirements: Map<SpecimenFieldType, FieldRequirement.Type>
    ): Boolean {
        return specimenFieldRequirements.any { (specimenField, fieldRequirement) ->
            if (fieldRequirement != FieldRequirement.Type.REQUIRED) {
                return@any false
            }

            when (specimenField) {
                SpecimenFieldType.SPECIMEN_HEADER -> false
                SpecimenFieldType.GENDER -> specimen.gender.isNullOr(Gender.UNKNOWN)
                SpecimenFieldType.AGE -> specimen.age.isNullOr(GameAge.UNKNOWN)
                SpecimenFieldType.WEIGHT -> specimen.weight == null
                SpecimenFieldType.WIDTH_OF_PAW -> specimen.widthOfPaw == null
                SpecimenFieldType.LENGTH_OF_PAW -> specimen.lengthOfPaw == null
                SpecimenFieldType.STATE_OF_HEALTH -> specimen.stateOfHealth == null
                SpecimenFieldType.MARKING -> specimen.marking == null
            }
        }
    }
}
