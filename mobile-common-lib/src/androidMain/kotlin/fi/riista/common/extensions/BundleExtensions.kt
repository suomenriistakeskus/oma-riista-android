package fi.riista.common.extensions

import android.os.Bundle
import fi.riista.common.domain.groupHunting.model.GroupHuntingDayId
import fi.riista.common.model.LocalDate
import fi.riista.common.model.extensions.fromEpochSeconds
import fi.riista.common.model.extensions.secondsFromEpoch
import fi.riista.common.util.deserializeFromJson
import fi.riista.common.util.serializeToJson

fun Bundle.getLongOrNull(key: String): Long? {
    // bundle will return the default value if stored value is not long or key does not exist.
    // We can check whether returned value is actually stored value by getting the value twice
    // with different default values
    val firstResult = getLong(key, 0)
    val secondResult = getLong(key, -1)

    return if (firstResult == secondResult) {
        firstResult
    } else {
        null
    }
}

fun Bundle.putGroupHuntingDayId(key: String, groupHuntingDayId: GroupHuntingDayId) {
    putLong(key, groupHuntingDayId.toLong())
}

fun Bundle.getGroupHuntingDayId(key: String): GroupHuntingDayId? {
    return getLongOrNull(key)?.let { GroupHuntingDayId.fromLong(it) }
}

fun Bundle.putLocalDate(key: String, date: LocalDate) {
    putLong(key, date.secondsFromEpoch())
}

fun Bundle.getLocalDate(key: String): LocalDate? {
    return getLongOrNull(key)?.let { LocalDate.fromEpochSeconds(it) }
}


// For data classes that have @Serializable annotation

inline fun <reified T> T.serializeToBundleAsJson(bundle: Bundle, key: String) {
    bundle.putString(key, this.serializeToJson())
}

inline fun <reified T> Bundle.deserializeJson(key: String): T? {
    return getString(key)?.deserializeFromJson()
}