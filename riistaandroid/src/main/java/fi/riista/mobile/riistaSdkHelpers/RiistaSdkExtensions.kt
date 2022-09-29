package fi.riista.mobile.riistaSdkHelpers

import android.content.Context
import androidx.annotation.StringRes
import fi.riista.common.domain.constants.SpeciesCode
import fi.riista.common.domain.season.HarvestSeasons
import fi.riista.common.model.*
import fi.riista.common.ui.dataField.DataFieldId
import fi.riista.common.ui.dataField.LabelField
import fi.riista.mobile.models.GeoLocation
import fi.riista.mobile.ui.dataFields.DataFieldRecyclerViewAdapter
import fi.riista.mobile.ui.dataFields.viewHolder.CaptionViewHolder
import fi.riista.mobile.ui.dataFields.viewHolder.DataFieldViewHolderType
import fi.riista.mobile.ui.dataFields.viewHolder.ErrorLabelViewHolder
import fi.riista.mobile.ui.dataFields.viewHolder.InfoViewHolder
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

fun LocalTime.toJodaLocalTime(): org.joda.time.LocalTime {
    return org.joda.time.LocalTime(hour, minute, second)
}

fun LocalTime.Companion.fromJodaLocalTime(time: org.joda.time.LocalTime): LocalTime {
    return LocalTime(
        hour = time.hourOfDay,
        minute = time.minuteOfHour,
        second = time.secondOfMinute,
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
    if (toTotalMinutes() == 0 && zeroMinutesStringRes != null) {
        return context.getString(zeroMinutesStringRes)
    }

    val stringProvider = ContextStringProviderFactory.createForContext(context)
    return formatHoursAndMinutesString(
        stringProvider = stringProvider,
    )
}

fun HarvestSeasons.isDuringHuntingSeason(
    speciesCode: SpeciesCode,
    date: org.joda.time.LocalDate
): Boolean {
    return isDuringHuntingSeason(speciesCode, LocalDate.fromJodaLocalDate(date))
}

fun GeoLocation.toETRMSGeoLocation() = ETRMSGeoLocation(
    latitude = latitude,
    longitude = longitude,
    source = source.toBackendEnum(),
    accuracy = accuracy,
    altitude = altitude,
    altitudeAccuracy = altitudeAccuracy,
)

fun ETRMSGeoLocation.toGeoLocation(): GeoLocation {
    return GeoLocation().also { location ->
        location.latitude = latitude
        location.longitude = longitude
        location.source = source.rawBackendEnumValue
        location.accuracy = accuracy
        location.altitude = altitude
        location.altitudeAccuracy = altitudeAccuracy
    }
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
