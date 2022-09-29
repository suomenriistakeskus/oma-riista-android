package fi.riista.common.domain.srva.model

import fi.riista.common.model.BackendEnum
import fi.riista.common.model.RepresentsBackendEnum
import fi.riista.common.model.toBackendEnum
import fi.riista.common.resources.LocalizableEnum
import fi.riista.common.resources.RR

enum class SrvaEventResultDetail(
    override val rawBackendEnumValue: String,
    override val resourcesStringId: RR.string,
): RepresentsBackendEnum, LocalizableEnum {
    // SrvaEventResult.ANIMAL_DEPORTED
    ANIMAL_CONTACTED_AND_DEPORTED("ANIMAL_CONTACTED_AND_DEPORTED", RR.string.srva_event_result_detail_animal_contacted_and_deported),
    ANIMAL_CONTACTED("ANIMAL_CONTACTED", RR.string.srva_event_result_detail_animal_contacted),
    UNCERTAIN_RESULT("UNCERTAIN_RESULT", RR.string.srva_event_result_detail_uncertain_result),

    ;

    // for iOS compatibility
    companion object {
        fun toBackendEnumCompat(value: String?): BackendEnum<SrvaEventResultDetail> = value.toBackendEnum()
    }
}
