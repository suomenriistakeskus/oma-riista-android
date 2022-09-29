package fi.riista.common.model

import fi.riista.common.resources.LocalizableEnum
import fi.riista.common.resources.RR


/**
 * Not really a backend enum but let's use that in order to have localization support
 * and help displaying values in UI
 */
internal enum class TrueOrFalse(
    override val rawBackendEnumValue: String,
    override val resourcesStringId: RR.string,
    val booleanValue: Boolean,
): RepresentsBackendEnum, LocalizableEnum {
    TRUE("1", RR.string.generic_yes, true),
    FALSE("0", RR.string.generic_no, false),
    ;
}

internal fun Boolean.toTrueOrFalseValue(): TrueOrFalse {
    return when (this) {
        true -> TrueOrFalse.TRUE
        false -> TrueOrFalse.FALSE
    }

}
