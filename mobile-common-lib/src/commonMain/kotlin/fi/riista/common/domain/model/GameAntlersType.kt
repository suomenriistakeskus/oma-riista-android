package fi.riista.common.domain.model

import fi.riista.common.model.BackendEnum
import fi.riista.common.model.RepresentsBackendEnum
import fi.riista.common.model.toBackendEnum
import fi.riista.common.resources.LocalizableEnum
import fi.riista.common.resources.RR

enum class GameAntlersType(
    override val rawBackendEnumValue: String,
    override val resourcesStringId: RR.string,
) : RepresentsBackendEnum, LocalizableEnum {

    CERVINE("HANKO", RR.string.harvest_antler_type_hanko),
    PALMATE("LAPIO", RR.string.harvest_antler_type_lapio),
    MIXED("SEKA", RR.string.harvest_antler_type_seka),
    ;

    // for iOS compatibility
    companion object {
        fun toBackendEnumCompat(value: String?): BackendEnum<GameAntlersType> = value.toBackendEnum()
    }
}
