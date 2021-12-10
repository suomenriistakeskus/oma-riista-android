package fi.riista.common.groupHunting.ui.groupHarvest.modify

import fi.riista.common.groupHunting.model.GroupHuntingDay
import fi.riista.common.groupHunting.model.GroupHuntingHarvestData
import fi.riista.common.groupHunting.model.HuntingGroupPermit
import fi.riista.common.groupHunting.model.isWithinPermit
import fi.riista.common.groupHunting.ui.GroupHarvestField
import fi.riista.common.logging.getLogger
import fi.riista.common.model.GameAge
import fi.riista.common.model.Gender
import fi.riista.common.ui.dataField.FieldSpecification
import fi.riista.common.util.isNullOr

object GroupHuntingHarvestValidator {
    enum class Error {
        MISSING_SPECIMEN,
        DATE_NOT_WITHIN_GROUP_PERMIT,
        MISSING_HUNTING_DAY,
        TIME_NOT_WITHIN_HUNTING_DAY,
        MISSING_DEER_HUNTING_TYPE,
        MISSING_DEER_HUNTING_OTHER_TYPE_DESCRIPTION,
        MISSING_ACTOR,
        MISSING_GENDER,
        MISSING_AGE,
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
    }

    private val logger by getLogger(GroupHuntingHarvestValidator::class)

    fun validate(
        harvest: GroupHuntingHarvestData,
        huntingDays: List<GroupHuntingDay>,
        huntingGroupPermit: HuntingGroupPermit,
        displayedFields: List<FieldSpecification<GroupHarvestField>>,
    ): List<Error> {
        val specimen = harvest.specimens.getOrNull(0)

        val missingSpecimenError = if (specimen == null) {
            listOf(Error.MISSING_SPECIMEN)
        } else {
            emptyList()
        }

        return missingSpecimenError + displayedFields.mapNotNull { fieldSpecification ->
            when (fieldSpecification.fieldId) {
                GroupHarvestField.DATE_AND_TIME -> {
                    fieldSpecification.ifRequired {
                        Error.DATE_NOT_WITHIN_GROUP_PERMIT.takeUnless {
                            harvest.pointOfTime.date.isWithinPermit(huntingGroupPermit)
                        }
                    }
                }
                GroupHarvestField.HUNTING_DAY_AND_TIME -> {
                    fieldSpecification.ifRequired {
                        validateHuntingDayAndTime(harvest, huntingDays)
                    }
                }
                GroupHarvestField.DEER_HUNTING_TYPE -> {
                    fieldSpecification.ifRequired {
                        Error.MISSING_DEER_HUNTING_TYPE.takeIf {
                            harvest.deerHuntingType.rawBackendEnumValue == null
                        }
                    }
                }
                GroupHarvestField.DEER_HUNTING_OTHER_TYPE_DESCRIPTION -> {
                    fieldSpecification.ifRequired {
                        Error.MISSING_DEER_HUNTING_OTHER_TYPE_DESCRIPTION.takeIf {
                            harvest.deerHuntingOtherTypeDescription == null
                        }
                    }
                }
                GroupHarvestField.ACTOR -> {
                    fieldSpecification.ifRequired {
                        Error.MISSING_ACTOR.takeIf {
                            harvest.actorInfo.personWithHunterNumber == null
                        }
                    }
                }
                GroupHarvestField.GENDER -> {
                    fieldSpecification.ifRequired {
                        Error.MISSING_GENDER.takeIf {
                            // require known gender
                            specimen != null && specimen.gender?.value.isNullOr(Gender.UNKNOWN)
                        }
                    }
                }
                GroupHarvestField.AGE -> {
                    fieldSpecification.ifRequired {
                        Error.MISSING_AGE.takeIf {
                            // require known age
                            specimen != null && specimen.age?.value.isNullOr(GameAge.UNKNOWN)
                        }
                    }
                }
                GroupHarvestField.ALONE -> {
                    fieldSpecification.ifRequired {
                        Error.MISSING_ALONE.takeIf {
                            specimen != null && specimen.alone == null
                        }
                    }
                }
                GroupHarvestField.NOT_EDIBLE -> {
                    fieldSpecification.ifRequired {
                        Error.MISSING_NOT_EDIBLE.takeIf {
                            specimen != null && specimen.notEdible == null
                        }
                    }
                }
                GroupHarvestField.WEIGHT_ESTIMATED -> {
                    fieldSpecification.ifRequired {
                        Error.MISSING_WEIGHT_ESTIMATED.takeIf {
                            specimen != null && specimen.weightEstimated == null
                        }
                    }
                }
                GroupHarvestField.WEIGHT_MEASURED -> {
                    fieldSpecification.ifRequired {
                        Error.MISSING_WEIGHT_MEASURED.takeIf {
                            specimen != null && specimen.weightMeasured == null
                        }
                    }
                }
                GroupHarvestField.FITNESS_CLASS -> {
                    fieldSpecification.ifRequired {
                        Error.MISSING_FITNESS_CLASS.takeIf {
                            specimen != null && specimen.fitnessClass?.rawBackendEnumValue == null
                        }
                    }

                }
                GroupHarvestField.ANTLERS_LOST -> {
                    fieldSpecification.ifRequired {
                        Error.MISSING_ANTLERS_LOST.takeIf {
                            specimen != null && specimen.antlersLost == null
                        }
                    }
                }
                GroupHarvestField.ANTLERS_TYPE -> {
                    fieldSpecification.ifRequired {
                        Error.MISSING_ANTLERS_TYPE.takeIf {
                            specimen != null && specimen.antlersType?.rawBackendEnumValue == null
                        }
                    }
                }
                GroupHarvestField.ANTLERS_WIDTH -> {
                    fieldSpecification.ifRequired {
                        Error.MISSING_ANTLERS_WIDTH.takeIf {
                            specimen != null && specimen.antlersWidth == null
                        }
                    }
                }
                GroupHarvestField.ANTLER_POINTS_LEFT -> {
                    fieldSpecification.ifRequired {
                        Error.MISSING_ANTLER_POINTS_LEFT.takeIf {
                            specimen != null && specimen.antlerPointsLeft == null
                        }
                    }
                }
                GroupHarvestField.ANTLER_POINTS_RIGHT -> {
                    fieldSpecification.ifRequired {
                        Error.MISSING_ANTLER_POINTS_RIGHT.takeIf {
                            specimen != null && specimen.antlerPointsRight == null
                        }
                    }
                }
                GroupHarvestField.ANTLERS_GIRTH -> {
                    fieldSpecification.ifRequired {
                        Error.MISSING_ANTLERS_GIRTH.takeIf {
                            specimen != null && specimen.antlersGirth == null
                        }
                    }
                }
                GroupHarvestField.ANTLER_SHAFT_WIDTH -> {
                    fieldSpecification.ifRequired {
                        Error.MISSING_ANTLER_SHAFT_WIDTH.takeIf {
                            specimen != null && specimen.antlerShaftWidth == null
                        }
                    }
                }
                GroupHarvestField.ANTLERS_LENGTH -> {
                    fieldSpecification.ifRequired {
                        Error.MISSING_ANTLERS_LENGTH.takeIf {
                            specimen != null && specimen.antlersLength == null
                        }
                    }
                }
                GroupHarvestField.ANTLERS_INNER_WIDTH -> {
                    fieldSpecification.ifRequired {
                        Error.MISSING_ANTLERS_INNER_WIDTH.takeIf {
                            specimen != null && specimen.antlersInnerWidth == null
                        }
                    }
                }
                GroupHarvestField.ADDITIONAL_INFORMATION -> {
                    fieldSpecification.ifRequired {
                        Error.MISSING_ADDITIONAL_INFORMATION.takeIf {
                            specimen != null && specimen.additionalInfo == null
                        }
                    }
                }

                // explicitly these don't need any validation
                GroupHarvestField.ACTOR_HUNTER_NUMBER,
                GroupHarvestField.ACTOR_HUNTER_NUMBER_INFO_OR_ERROR,
                GroupHarvestField.ANTLER_INSTRUCTIONS,
                GroupHarvestField.ADDITIONAL_INFORMATION_INSTRUCTIONS,
                GroupHarvestField.ERROR_DATE_NOT_WITHIN_GROUP_PERMIT,
                GroupHarvestField.ERROR_TIME_NOT_WITHIN_HUNTING_DAY,
                GroupHarvestField.SPECIES_CODE,
                GroupHarvestField.LOCATION,
                GroupHarvestField.HEADLINE_SHOOTER,
                GroupHarvestField.AUTHOR,
                GroupHarvestField.HEADLINE_SPECIMEN,
                GroupHarvestField.WEIGHT -> {
                    null
                }
            }
        }.also { errors ->
            if (errors.isEmpty()) {
                logger.v { "GroupHuntingHarvest is valid!" }
            } else {
                logger.d { "GroupHuntingHarvest validation errors: $errors" }
            }
        }
    }

    private fun validateHuntingDayAndTime(
        harvest: GroupHuntingHarvestData,
        huntingDays: List<GroupHuntingDay>
    ): Error? {
        val huntingDay = harvest.huntingDayId?.let { dayId ->
            huntingDays.find { it.id == dayId }
        } ?: kotlin.run {
            return Error.MISSING_HUNTING_DAY
        }

        return if (harvest.pointOfTime < huntingDay.startDateTime ||
            harvest.pointOfTime > huntingDay.endDateTime) {
            Error.TIME_NOT_WITHIN_HUNTING_DAY
        } else {
            null
        }
    }

    private fun <R> FieldSpecification<GroupHarvestField>.ifRequired(block: () -> R?): R? {
        return if (requirementStatus.isRequired()) {
            block()
        } else {
            null
        }
    }
}
