package fi.riista.mobile.models

import java.util.*

object GameLog {

    const val TYPE_HARVEST = "HARVEST"
    const val TYPE_OBSERVATION = "OBSERVATION"
    const val TYPE_SRVA = "SRVA"
    const val TYPE_POI = "POI"

    const val LOCATION_SOURCE_MANUAL = "MANUAL"
    const val LOCATION_SOURCE_GPS = "GPS_DEVICE"

    const val SPECIMEN_DETAILS_MAX = 25

    private val random = Random()

    @JvmStatic
    fun generateMobileRefId(): Long {
        val calendar: Calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        val secondsSinceEpoch = calendar.timeInMillis / 1000L
        return (secondsSinceEpoch shl 32) + random.nextInt()
    }
}
