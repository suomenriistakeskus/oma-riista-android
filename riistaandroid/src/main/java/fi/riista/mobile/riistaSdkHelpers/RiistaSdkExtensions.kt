package fi.riista.mobile.riistaSdkHelpers

import android.content.Context
import androidx.annotation.StringRes
import fi.riista.common.domain.season.HarvestSeasons
import fi.riista.common.model.HoursAndMinutes
import fi.riista.common.model.LocalDate
import fi.riista.common.model.LocalDateTime
import fi.riista.common.model.SpeciesCode
import fi.riista.common.ui.dataField.DataFieldId
import fi.riista.common.ui.dataField.LabelField
import fi.riista.mobile.R
import fi.riista.mobile.feature.groupHunting.dataFields.DataFieldRecyclerViewAdapter
import fi.riista.mobile.feature.groupHunting.dataFields.viewHolder.CaptionViewHolder
import fi.riista.mobile.feature.groupHunting.dataFields.viewHolder.DataFieldViewHolderType
import fi.riista.mobile.feature.groupHunting.dataFields.viewHolder.ErrorLabelViewHolder
import fi.riista.mobile.feature.groupHunting.dataFields.viewHolder.InfoViewHolder
import org.joda.time.DateTime

fun LocalDate.toJodaLocalDate(): org.joda.time.LocalDate {
    return org.joda.time.LocalDate(year, monthNumber, dayOfMonth)
}

fun LocalDate.Companion.fromJodaLocalDate(date: org.joda.time.LocalDate): LocalDate {
    return LocalDate(
            year = date.year,
            monthNumber = date.monthOfYear,
            dayOfMonth = date.dayOfMonth,
    )
}

fun LocalDateTime.toJodaDateTime(): DateTime {
    return DateTime(year, monthNumber, dayOfMonth, hour, minute, second)
}

fun LocalDateTime.Companion.fromJodaDateTime(dateTime: DateTime): LocalDateTime {
    return LocalDateTime(
            year = dateTime.year,
            monthNumber = dateTime.monthOfYear,
            dayOfMonth = dateTime.dayOfMonth,
            hour = dateTime.hourOfDay,
            minute = dateTime.minuteOfHour,
            second = dateTime.secondOfMinute,
    )
}


/**
 * Formats the duration to hours and minutes string with an optional special case
 * handling when the total duration is 0 minutes.
 */
fun HoursAndMinutes.formatToHoursAndMinutesString(
    context: Context,
    @StringRes zeroMinutesStringRes: Int? = null,
): String {
    val zeroMinutes = toTotalMinutes() == 0

    val hoursText = if (hours > 0 || zeroMinutes) {
        context.resources.getQuantityString(R.plurals.hours, hours, hours)
    } else {
        ""
    }
    val minutesText = if (minutes > 0 || zeroMinutes) {
        context.resources.getQuantityString(R.plurals.minutes, minutes, minutes)
    } else {
        ""
    }

    return when {
        zeroMinutes && zeroMinutesStringRes != null ->
            context.getString(zeroMinutesStringRes)
        hoursText.isNotBlank() && minutesText.isNotBlank() ->
            context.getString(R.string.hours_and_minutes, hoursText, minutesText)
        hoursText.isNotBlank() -> hoursText
        minutesText.isNotBlank() -> minutesText
        else -> ""
    }
}

fun HarvestSeasons.isDuringHuntingSeason(
    speciesCode: SpeciesCode,
    date: org.joda.time.LocalDate
): Boolean {
    return isDuringHuntingSeason(speciesCode, LocalDate.fromJodaLocalDate(date))
}

// data field helpers

fun <T : DataFieldId> LabelField<T>.determineViewHolderType(): DataFieldViewHolderType {
    return when (type) {
        LabelField.Type.CAPTION -> DataFieldViewHolderType.LABEL_CAPTION
        LabelField.Type.ERROR -> DataFieldViewHolderType.LABEL_ERROR
        LabelField.Type.INFO -> DataFieldViewHolderType.LABEL_INFO
    }
}

fun <FieldId : DataFieldId> DataFieldRecyclerViewAdapter<FieldId>.registerLabelFieldViewHolderFactories() {
    registerViewHolderFactory(CaptionViewHolder.Factory())
    registerViewHolderFactory(ErrorLabelViewHolder.Factory())
    registerViewHolderFactory(InfoViewHolder.Factory())
}
