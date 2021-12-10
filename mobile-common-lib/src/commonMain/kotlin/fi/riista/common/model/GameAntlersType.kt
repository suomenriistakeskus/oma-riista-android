package fi.riista.common.model

import fi.riista.common.resources.LocalizableEnum
import fi.riista.common.resources.RR
import fi.riista.common.resources.RStringId

enum class GameAntlersType(
    override val rawBackendEnumValue: String,
    override val resourcesStringId: RStringId,
) : RepresentsBackendEnum, LocalizableEnum {

    CERVINE("HANKO", RR.string.harvest_antler_type_hanko),
    PALMATE("LAPIO", RR.string.harvest_antler_type_lapio),
    MIXED("SEKA", RR.string.harvest_antler_type_seka),
    ;
}
