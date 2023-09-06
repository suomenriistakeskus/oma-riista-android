package fi.riista.common.domain.groupHunting.ui.groupHarvest.validation

import fi.riista.common.domain.content.SpeciesResolver
import fi.riista.common.domain.groupHunting.model.GroupHuntingDay
import fi.riista.common.domain.groupHunting.model.HuntingGroupPermit
import fi.riista.common.domain.groupHunting.model.isWithinPermit
import fi.riista.common.domain.harvest.model.CommonHarvestData
import fi.riista.common.domain.harvest.model.HarvestReportingType
import fi.riista.common.domain.harvest.ui.CommonHarvestField
import fi.riista.common.domain.harvest.validation.CommonHarvestValidator
import fi.riista.common.ui.dataField.FieldSpecification
import fi.riista.common.util.LocalDateTimeProvider
import fi.riista.common.util.containsAny


class GroupHarvestValidator(
    localDateTimeProvider: LocalDateTimeProvider,
    speciesResolver: SpeciesResolver,
) {
    private val commonHarvestValidator = CommonHarvestValidator(localDateTimeProvider, speciesResolver)

    internal fun validate(
        harvest: CommonHarvestData,
        huntingDays: List<GroupHuntingDay>,
        huntingGroupPermit: HuntingGroupPermit,
        displayedFields: List<FieldSpecification<CommonHarvestField>>,
    ): List<CommonHarvestValidator.Error> {
        val commonErrors = commonHarvestValidator.validate(
            harvest = harvest,
            permit = null, // no common permit, handle separately
            harvestReportingType = HarvestReportingType.SEASON,
            displayedFields = displayedFields
        )

        return commonErrors + displayedFields
            .mapNotNull { fieldSpecification ->
                when (fieldSpecification.fieldId) {
                    CommonHarvestField.DATE_AND_TIME -> {
                        val dateTimeErrorExists = commonErrors.containsAny(
                            others = listOf(
                                CommonHarvestValidator.Error.MISSING_DATE_AND_TIME,
                                CommonHarvestValidator.Error.DATE_NOT_WITHIN_PERMIT,
                                CommonHarvestValidator.Error.DATETIME_IN_FUTURE,
                            )
                        )
                        if (dateTimeErrorExists) {
                            // don't display multiple datetime errors
                            return@mapNotNull null
                        }

                        if (!harvest.pointOfTime.date.isWithinPermit(huntingGroupPermit)) {
                            CommonHarvestValidator.Error.DATE_NOT_WITHIN_PERMIT
                        } else {
                            null
                        }
                    }
                    CommonHarvestField.HUNTING_DAY_AND_TIME -> {
                        fieldSpecification.ifRequired {
                            validateHuntingDayAndTime(harvest, huntingDays)
                        }
                    }
                    else -> null
                }
            }
    }

    private fun validateHuntingDayAndTime(
        harvest: CommonHarvestData,
        huntingDays: List<GroupHuntingDay>
    ): CommonHarvestValidator.Error? {
        val huntingDay = harvest.huntingDayId?.let { dayId ->
            huntingDays.find { it.id == dayId }
        } ?: kotlin.run {
            return CommonHarvestValidator.Error.MISSING_HUNTING_DAY
        }

        return if (harvest.pointOfTime < huntingDay.startDateTime ||
            harvest.pointOfTime > huntingDay.endDateTime) {
            CommonHarvestValidator.Error.TIME_NOT_WITHIN_HUNTING_DAY
        } else {
            null
        }
    }
}