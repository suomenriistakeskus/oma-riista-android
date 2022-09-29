package fi.riista.common.domain.model

import fi.riista.common.model.BackendEnum
import fi.riista.common.model.RepresentsBackendEnum
import fi.riista.common.model.toBackendEnum
import fi.riista.common.resources.LocalizableEnum
import fi.riista.common.resources.RR

enum class ObservationCategory(
    override val rawBackendEnumValue: String,
    override val resourcesStringId: RR.string
): RepresentsBackendEnum, LocalizableEnum {
    NORMAL("NORMAL", RR.string.observation_category_normal),
    MOOSE_HUNTING("MOOSE_HUNTING", RR.string.observation_category_moose_hunting),
    DEER_HUNTING("DEER_HUNTING", RR.string.observation_category_deer_hunting),
    ;

    // for iOS compatibility
    companion object {
        fun toBackendEnumCompat(value: String?): BackendEnum<ObservationCategory> = value.toBackendEnum()
    }
}
