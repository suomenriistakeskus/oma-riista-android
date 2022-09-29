package fi.riista.common.domain.model

import fi.riista.common.model.BackendEnum
import fi.riista.common.model.RepresentsBackendEnum
import fi.riista.common.model.toBackendEnum
import fi.riista.common.resources.LocalizableEnum
import fi.riista.common.resources.RR

enum class DeerHuntingType(
    override val rawBackendEnumValue: String,
    override val resourcesStringId: RR.string,
) : RepresentsBackendEnum, LocalizableEnum {
    STAND_HUNTING("STAND_HUNTING", RR.string.deer_hunting_type_stand_hunting),
    DOG_HUNTING("DOG_HUNTING", RR.string.deer_hunting_type_dog_hunting),
    OTHER("OTHER", RR.string.deer_hunting_type_other),
    ;

    // for iOS compatibility
    companion object {
        fun toBackendEnumCompat(value: String?): BackendEnum<DeerHuntingType> = value.toBackendEnum()
    }
}
