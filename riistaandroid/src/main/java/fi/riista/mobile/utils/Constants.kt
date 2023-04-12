package fi.riista.mobile.utils

import android.location.Location

const val ROOM_DATABASE_NAME = "riista_room.db"
const val TIMEZONE_ID_FINLAND = "Europe/Helsinki"

const val ISO_8601 = "yyyy-MM-dd'T'HH:mm:ss.SSS"
const val ISO_8601_NO_MILLISECONDS = "yyyy-MM-dd'T'HH:mm:ss"

const val DATE_FORMAT_STD = "yyyy-MM-dd"
const val DATE_FORMAT_FINNISH_SHORT = "d.M.yyyy"
const val DATE_FORMAT_FINNISH_LONG = "dd.MM.yyyy"

const val TIME_FORMAT = "HH:mm"

const val IMAGE_CACHE_SIZE: Long = 1024 * 1024 * 128 // 128 MB
const val VECTOR_CACHE_SIZE: Long = 1024 * 1024 * 128 // 128 MB

// center point of Finland
object Constants {
    val DEFAULT_MAP_LOCATION = Location("").apply {
        // center point of Finland
        latitude = 64.10
        longitude = 25.48
    }
    const val DEFAULT_MAP_ZOOM_LEVEL: Float = 5.5F

    // Disabled UI element alpha in range [0..255]
    const val DISABLED_ALPHA: Int = 128
}