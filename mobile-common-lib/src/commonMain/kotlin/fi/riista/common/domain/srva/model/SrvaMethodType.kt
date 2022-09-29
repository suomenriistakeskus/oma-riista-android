package fi.riista.common.domain.srva.model

import fi.riista.common.model.BackendEnum
import fi.riista.common.model.RepresentsBackendEnum
import fi.riista.common.model.toBackendEnum
import fi.riista.common.resources.LocalizableEnum
import fi.riista.common.resources.RR

enum class SrvaMethodType(
    override val rawBackendEnumValue: String,
    override val resourcesStringId: RR.string
): RepresentsBackendEnum, LocalizableEnum {
    // Accident / Injured animal
    TRACED_WITH_DOG("TRACED_WITH_DOG", RR.string.srva_method_traced_with_dog),
    TRACED_WITHOUT_DOG("TRACED_WITHOUT_DOG", RR.string.srva_method_traced_without_dog),

    // Deportation
    DOG("DOG", RR.string.srva_method_dog),
    PAIN_EQUIPMENT("PAIN_EQUIPMENT", RR.string.srva_method_pain_equipment),
    SOUND_EQUIPMENT("SOUND_EQUIPMENT", RR.string.srva_method_sound_equipment),
    VEHICLE("VEHICLE", RR.string.srva_method_vehicle),
    CHASING_WITH_PEOPLE("CHASING_WITH_PEOPLE", RR.string.srva_method_chasing_with_people),

    // Common
    OTHER("OTHER", RR.string.srva_method_other),

    ;

    // for iOS compatibility
    companion object {
        fun toBackendEnumCompat(value: String?): BackendEnum<SrvaMethodType> = value.toBackendEnum()
    }
}
