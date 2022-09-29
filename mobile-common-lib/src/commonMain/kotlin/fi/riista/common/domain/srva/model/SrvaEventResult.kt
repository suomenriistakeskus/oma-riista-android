package fi.riista.common.domain.srva.model

import fi.riista.common.model.BackendEnum
import fi.riista.common.model.RepresentsBackendEnum
import fi.riista.common.model.toBackendEnum
import fi.riista.common.resources.LocalizableEnum
import fi.riista.common.resources.RR

enum class SrvaEventResult(
    override val rawBackendEnumValue: String,
    override val resourcesStringId: RR.string,
): RepresentsBackendEnum, LocalizableEnum {
    ANIMAL_FOUND_DEAD("ANIMAL_FOUND_DEAD", RR.string.srva_event_result_animal_found_dead),
    ANIMAL_FOUND_AND_TERMINATED("ANIMAL_FOUND_AND_TERMINATED", RR.string.srva_event_result_animal_found_and_terminated),
    ANIMAL_FOUND_AND_NOT_TERMINATED("ANIMAL_FOUND_AND_NOT_TERMINATED", RR.string.srva_event_result_animal_found_and_not_terminated),
    ACCIDENT_SITE_NOT_FOUND("ACCIDENT_SITE_NOT_FOUND", RR.string.srva_event_result_accident_site_not_found),
    ANIMAL_NOT_FOUND("ANIMAL_NOT_FOUND", RR.string.srva_event_result_animal_not_found),

    ANIMAL_TERMINATED("ANIMAL_TERMINATED", RR.string.srva_event_result_animal_terminated),
    ANIMAL_DEPORTED("ANIMAL_DEPORTED", RR.string.srva_event_result_animal_deported),

    UNDUE_ALARM("UNDUE_ALARM", RR.string.srva_event_result_undue_alarm),

    ;

    // for iOS compatibility
    companion object {
        fun toBackendEnumCompat(value: String?): BackendEnum<SrvaEventResult> = value.toBackendEnum()
    }
}
