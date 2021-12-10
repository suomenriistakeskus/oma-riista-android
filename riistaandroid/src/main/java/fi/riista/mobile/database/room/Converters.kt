package fi.riista.mobile.database.room

import androidx.room.TypeConverter
import fi.riista.mobile.utils.DateTimeUtils.JODA_TZ_FINLAND
import org.joda.time.LocalDate

class Converters {

    @TypeConverter
    fun fromEpochMillisToLocalDate(value: Long?): LocalDate? {
        return value?.let { LocalDate(it, JODA_TZ_FINLAND) }
    }

    @TypeConverter
    fun fromLocalDateToEpochMillis(date: LocalDate?): Long? {
        return date?.let { it.toDateTimeAtStartOfDay(JODA_TZ_FINLAND).millis }
    }
}
