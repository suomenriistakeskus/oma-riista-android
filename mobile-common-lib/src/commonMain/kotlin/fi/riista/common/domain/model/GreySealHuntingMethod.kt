package fi.riista.common.domain.model

import fi.riista.common.model.BackendEnum
import fi.riista.common.model.RepresentsBackendEnum
import fi.riista.common.model.toBackendEnum
import fi.riista.common.resources.LocalizableEnum
import fi.riista.common.resources.RR

enum class GreySealHuntingMethod(
    override val rawBackendEnumValue: String,
    override val resourcesStringId: RR.string,
) : RepresentsBackendEnum, LocalizableEnum {
    SHOT("SHOT", RR.string.grey_seal_hunting_method_shot),
    CAPTURED_ALIVE("CAPTURED_ALIVE", RR.string.grey_seal_hunting_method_captured_alive),
    SHOT_BUT_LOST("SHOT_BUT_LOST", RR.string.grey_seal_hunting_method_shot_but_lost),
    ;

    // for iOS compatibility
    companion object {
        fun toBackendEnumCompat(value: String?): BackendEnum<GreySealHuntingMethod> = value.toBackendEnum()
    }
}
