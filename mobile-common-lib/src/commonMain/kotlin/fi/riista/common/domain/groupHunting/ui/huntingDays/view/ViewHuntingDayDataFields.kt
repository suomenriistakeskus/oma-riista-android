package fi.riista.common.domain.groupHunting.ui.huntingDays.view

import fi.riista.common.domain.groupHunting.ui.huntingDays.HuntingDayDiaryEntryViewModel
import fi.riista.common.domain.groupHunting.ui.huntingDays.HuntingDayHarvestViewModel
import fi.riista.common.domain.groupHunting.ui.huntingDays.HuntingDayObservationViewModel
import fi.riista.common.domain.groupHunting.ui.huntingDays.HuntingDayViewModel
import fi.riista.common.domain.groupHunting.ui.huntingDays.view.ViewHuntingDayField.Type
import fi.riista.common.domain.model.DeerHuntingType
import fi.riista.common.model.HoursAndMinutes
import fi.riista.common.model.formatHoursAndMinutesString
import fi.riista.common.resources.RR
import fi.riista.common.resources.StringProvider
import fi.riista.common.resources.localized
import fi.riista.common.ui.dataField.*

/**
 * A class that helps displaying hunting day data using DataFields.
 *
 * Used on iOS. Do not remove.
 */
@Suppress("unused")
class ViewHuntingDayDataFields(
    private val stringProvider: StringProvider,
) {
    var huntingDayFields: List<DataField<ViewHuntingDayField>> = listOf()
        private set

    fun updateFields(huntingDayViewModel: HuntingDayViewModel) {
        huntingDayFields = listOfNotNull(
            DateAndTimeField(
                id = ViewHuntingDayField(Type.START_DATE_AND_TIME),
                dateAndTime = huntingDayViewModel.huntingDay.startDateTime,
            ) {
                paddingTop = Padding.MEDIUM_LARGE
                label = stringProvider.getString(RR.string.group_hunting_day_label_start_date_and_time)
            },
            DateAndTimeField(
                id = ViewHuntingDayField(Type.END_DATE_AND_TIME),
                dateAndTime = huntingDayViewModel.huntingDay.endDateTime,
            ) {
                label = stringProvider.getString(RR.string.group_hunting_day_label_end_date_and_time)
            },
            CustomUserInterfaceField(
                id = ViewHuntingDayField(Type.ACTION_CREATE_HUNTING_DAY)
            ) {
                paddingTop = Padding.SMALL
                paddingBottom = Padding.MEDIUM_LARGE
            }.takeIf {
                // hunting day can only be created if it doesn't exist yet i.e.
                // it is locally created hunting day
                huntingDayViewModel.canCreateHuntingDay
            },
            huntingDayViewModel.huntingDay.numberOfHunters
                .createLabelAndValue(
                    type = Type.NUMBER_OF_HUNTERS,
                    labelId = RR.string.group_hunting_day_label_number_of_hunters,
                    condition = { huntingDayViewModel.showHuntingDayDetails }
                ),
            huntingDayViewModel.huntingDay.huntingMethod
                .localized(stringProvider)
                .createLabelAndValue(
                    type = Type.HUNTING_METHOD,
                    labelId = RR.string.group_hunting_day_label_hunting_method,
                    condition = { huntingDayViewModel.showHuntingDayDetails }
                ),
            huntingDayViewModel.huntingDay.numberOfHounds
                .createLabelAndValue(
                    type = Type.NUMBER_OF_HOUNDS,
                    labelId = RR.string.group_hunting_day_label_number_of_hounds,
                    condition = { huntingDayViewModel.showHuntingDayDetails }
                ),
            huntingDayViewModel.huntingDay.snowDepth
                .createLabelAndValue(
                    type = Type.SNOW_DEPTH,
                    labelId = RR.string.group_hunting_day_label_snow_depth_centimeters,
                    condition = { huntingDayViewModel.showHuntingDayDetails }
                ),
            huntingDayViewModel.huntingDay.breakDurationInMinutes
                ?.let { minutes ->
                    HoursAndMinutes(minutes)
                }
                ?.formatHoursAndMinutesString(
                    stringProvider, RR.string.group_hunting_day_no_breaks
                )
                .createLabelAndValue(
                    type = Type.BREAK_DURATION,
                    labelId = RR.string.group_hunting_day_label_break_duration_minutes,
                    paddingBottom = Padding.LARGE,
                    condition = { huntingDayViewModel.showHuntingDayDetails }
                ),
        ) + createHarvestAndObservationFields(huntingDayViewModel)
    }


    private fun createHarvestAndObservationFields(
        huntingDayViewModel: HuntingDayViewModel
    ): List<DataField<ViewHuntingDayField>> {
        val entriesByDeerHuntingType: Map<DeerHuntingType?, List<HuntingDayDiaryEntryViewModel>> =
            (huntingDayViewModel.harvests + huntingDayViewModel.observations)
                .groupBy { it.deerHuntingType }

        // section labels should only be shown if there are known deer hunting types
        val showDeerHuntingTypeLabels = DeerHuntingType.values().find {
            entriesByDeerHuntingType.contains(it)
        } != null

        return DeerHuntingType.values().flatMap { deerHuntingType ->
            val entries = when (deerHuntingType) {
                DeerHuntingType.OTHER -> {
                    // include unknown (== null) deer hunting types under "OTHER" category
                    (entriesByDeerHuntingType[deerHuntingType] ?: listOf()) +
                            (entriesByDeerHuntingType[null] ?: listOf())
                }
                else -> entriesByDeerHuntingType[deerHuntingType] ?: listOf()
            }.sortedBy { it.pointOfTime }

            if (entries.isEmpty()) {
                return@flatMap emptyList()
            }

            listOfNotNull(
                LabelField(
                    id = ViewHuntingDayField(type = Type.SECTION_HEADER, id = deerHuntingType.ordinal.toLong()),
                    text = stringProvider.getString(deerHuntingType.resourcesStringId),
                    type = LabelField.Type.CAPTION
                ) {
                    paddingTop = Padding.MEDIUM_LARGE
                }.takeIf { showDeerHuntingTypeLabels }
            ) + entries.mapNotNull { entryViewModel ->
                when (entryViewModel) {
                    is HuntingDayHarvestViewModel -> entryViewModel.toHarvestField()
                    is HuntingDayObservationViewModel -> entryViewModel.toObservationField()
                    else -> null
                }
            }
        }
    }

    private fun Any?.createLabelAndValue(
        type: Type,
        labelId: RR.string,
        paddingBottom: Padding = Padding.SMALL,
        condition: (() -> Boolean)? = null,
    ): StringField<ViewHuntingDayField>? {
        if (condition != null && !condition()) {
            return null
        }
        val value = this?.toString() ?: "-"

        return StringField(
            id = ViewHuntingDayField(type),
            value = value
        ) {
            singleLine = true
            readOnly = true
            label = stringProvider.getString(labelId)
            paddingTop = Padding.SMALL
            this.paddingBottom = paddingBottom
        }
    }
}

fun HuntingDayHarvestViewModel.toHarvestField(): HarvestField<ViewHuntingDayField> {
    val harvestId = id

    return HarvestField(
        id = ViewHuntingDayField(type = Type.HARVEST, id = harvestId),
        harvestId = harvestId,
        speciesCode = speciesCode,
        pointOfTime = pointOfTime,
        amount = amount,
        acceptStatus = acceptStatus,
    ) {
        paddingTop = Padding.NONE
        paddingBottom = Padding.NONE
    }
}

fun HuntingDayObservationViewModel.toObservationField(): ObservationField<ViewHuntingDayField> {
    val observationId = id

    return ObservationField(
        id = ViewHuntingDayField(type = Type.OBSERVATION, id = observationId),
        observationId = observationId,
        speciesCode = speciesCode,
        pointOfTime = pointOfTime,
        amount = amount,
        acceptStatus = acceptStatus,
    ) {
        paddingTop = Padding.NONE
        paddingBottom = Padding.NONE
    }
}