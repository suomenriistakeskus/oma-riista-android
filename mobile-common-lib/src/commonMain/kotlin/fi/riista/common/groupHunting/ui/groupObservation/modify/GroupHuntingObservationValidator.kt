package fi.riista.common.groupHunting.ui.groupObservation.modify

import fi.riista.common.groupHunting.model.GroupHuntingDay
import fi.riista.common.groupHunting.model.GroupHuntingObservationData
import fi.riista.common.groupHunting.ui.GroupObservationField
import fi.riista.common.model.SpeciesCodes
import fi.riista.common.ui.dataField.FieldSpecification

object GroupHuntingObservationValidator {
    enum class Error {
        MISSING_HUNTING_DAY,
        TIME_NOT_WITHIN_HUNTING_DAY,
        MISSING_ACTOR,
        MISSING_SPECIMEN_AMOUNT,
        TOTAL_SPECIMEN_AMOUNT_IS_ZERO,
    }

    fun validate(
        observation: GroupHuntingObservationData,
        huntingDays: List<GroupHuntingDay>,
        displayedFields: List<FieldSpecification<GroupObservationField>>,
    ): List<Error> {

        val missingSpecimenAmount = if (specimenAmountsAreDefined(observation)) {
            emptyList()
        } else {
            listOf(Error.MISSING_SPECIMEN_AMOUNT)
        }
        val totalSpecimenAmount = if (totalSpecimenAmount(observation) == 0) {
            listOf(Error.TOTAL_SPECIMEN_AMOUNT_IS_ZERO)
        } else {
            emptyList()
        }

        return missingSpecimenAmount +
                totalSpecimenAmount +
                displayedFields.mapNotNull { fieldSpecification ->
            when (fieldSpecification.fieldId) {
                GroupObservationField.HUNTING_DAY_AND_TIME -> {
                    validateHuntingDayAndTime(observation, huntingDays)
                }
                GroupObservationField.ACTOR -> {
                    fieldSpecification.ifRequired {
                        Error.MISSING_ACTOR.takeIf {
                            observation.actorInfo.personWithHunterNumber == null
                        }
                    }
                }

                else -> null
            }
        }
    }

    private fun validateHuntingDayAndTime(
        observation: GroupHuntingObservationData,
        huntingDays: List<GroupHuntingDay>
    ): Error? {
        val huntingDay = observation.huntingDayId?.let { dayId ->
            huntingDays.find { it.id == dayId }
        } ?: kotlin.run {
            return Error.MISSING_HUNTING_DAY
        }

        return if (observation.pointOfTime < huntingDay.startDateTime ||
            observation.pointOfTime > huntingDay.endDateTime) {
            Error.TIME_NOT_WITHIN_HUNTING_DAY
        } else {
            null
        }
    }

    private fun isGroupObservationValid(observation: GroupHuntingObservationData): Boolean {
        return observation.huntingDayId != null &&
                observation.actorInfo.personWithHunterNumber != null &&
                specimenAmountsAreDefined(observation) &&
                totalSpecimenAmount(observation) > 0
    }

    private fun totalSpecimenAmount(observation: GroupHuntingObservationData): Int {
        return (observation.mooselikeMaleAmount ?: 0) +
                (observation.mooselikeFemaleAmount ?: 0) +
                (observation.mooselikeFemale1CalfAmount ?: 0) +
                (observation.mooselikeFemale2CalfsAmount ?: 0) +
                (observation.mooselikeFemale3CalfsAmount ?: 0) +
                (observation.mooselikeFemale4CalfsAmount ?: 0) +
                (observation.mooselikeCalfAmount ?: 0) +
                (observation.mooselikeUnknownSpecimenAmount ?: 0)
    }

    private fun specimenAmountsAreDefined(observation: GroupHuntingObservationData): Boolean {
        return (observation.mooselikeMaleAmount != null) &&
                (observation.mooselikeFemaleAmount != null) &&
                (observation.mooselikeFemale1CalfAmount != null) &&
                (observation.mooselikeFemale2CalfsAmount != null) &&
                (observation.mooselikeFemale3CalfsAmount != null) &&
                (observation.mooselikeFemale4CalfsAmount != null ||
                        (observation.gameSpeciesCode != SpeciesCodes.WHITE_TAILED_DEER_ID)) &&
                (observation.mooselikeCalfAmount != null) &&
                (observation.mooselikeUnknownSpecimenAmount != null)
    }

    private fun <R> FieldSpecification<GroupObservationField>.ifRequired(block: () -> R?): R? {
        return if (requirementStatus.isRequired()) {
            block()
        } else {
            null
        }
    }
}
