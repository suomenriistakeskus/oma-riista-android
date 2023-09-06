package fi.riista.common.domain.model

import fi.riista.common.model.RepresentsBackendEnum
import fi.riista.common.resources.LocalizableEnum
import fi.riista.common.resources.RR

enum class ShootingTestType(
    override val rawBackendEnumValue: String,
    override val resourcesStringId: RR.string,
) : RepresentsBackendEnum, LocalizableEnum {
    MOOSE("MOOSE", RR.string.shooting_test_type_moose),
    BEAR("BEAR", RR.string.shooting_test_type_bear),
    ROE_DEER("ROE_DEER", RR.string.shooting_test_type_roe_deer),
    BOW("BOW", RR.string.shooting_test_type_bow),
    ;
}
