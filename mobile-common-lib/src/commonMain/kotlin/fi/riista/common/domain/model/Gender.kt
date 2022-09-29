package fi.riista.common.domain.model

import fi.riista.common.model.BackendEnum
import fi.riista.common.model.RepresentsBackendEnum
import fi.riista.common.model.toBackendEnum
import fi.riista.common.resources.LocalizableEnum
import fi.riista.common.resources.RR

enum class Gender(
    override val rawBackendEnumValue: String,
    override val resourcesStringId: RR.string,
): RepresentsBackendEnum, LocalizableEnum {
    FEMALE("FEMALE", RR.string.gender_female),
    MALE("MALE", RR.string.gender_male),
    UNKNOWN("UNKNOWN", RR.string.gender_unknown),
    ;

    // for iOS compatibility
    companion object {
        fun toBackendEnumCompat(value: String?): BackendEnum<Gender> = value.toBackendEnum()
    }
}
