package fi.riista.common.domain.srva.model

import fi.riista.common.model.BackendEnum
import fi.riista.common.model.RepresentsBackendEnum
import fi.riista.common.model.toBackendEnum
import fi.riista.common.resources.LocalizableEnum
import fi.riista.common.resources.RR

enum class SrvaEventCategoryType(
    override val rawBackendEnumValue: String,
    override val resourcesStringId: RR.string
): RepresentsBackendEnum, LocalizableEnum {
    ACCIDENT("ACCIDENT", RR.string.srva_event_category_accident),
    DEPORTATION("DEPORTATION", RR.string.srva_event_category_deportation),
    INJURED_ANIMAL("INJURED_ANIMAL", RR.string.srva_event_category_injured_animal),

    ;

    // for iOS compatibility
    companion object {
        fun toBackendEnumCompat(value: String?): BackendEnum<SrvaEventCategoryType> = value.toBackendEnum()
    }
}